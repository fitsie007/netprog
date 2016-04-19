/*
 *	Command-Line Socket Interface
 *
 *	(c) 1998--2001 Martin Mares <mj@atrey.karlin.mff.cuni.cz>
 *
 *	This program can be freely distributed and used according
 *	to the terms of the GNU General Public Licence.
 */

#include "config.h"

#include <sys/types.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdarg.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/un.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/time.h>

#define SOCK_VERSION "1.1"

static char *opts = "vtuxb:lden";

static enum { ip, ux } addr = ip;
static enum { deflt, tcp, udp, uxp } proto = deflt;
static int listen_p, verbose, daemon_p, single_eof_p, avoid_dns_p;
static char *cmd, *my_name;

static struct sockaddr *sa_bind, *sa_conn;
static char *na_bind, *na_conn;
static int sock_addr_length;

#ifdef __GNUC__
#define NORET __attribute__((noreturn))
#else
#define NORET
#endif

static void usage(void) NORET;
static void mdie(char *, ...) NORET;
static void die(char *, ...) NORET;

static void
usage(void)
{
  fprintf(stderr, "\
Usage: %s [-vtuxde] ([-b <local>] <remote> | -l <local>) [<command>]\n\
\n\
-v\tBe verbose\n\
-t\tUse TCP socket (default)\n\
-u\tUse UDP socket\n\
-x\tUse UNIX-domain socket\n\
-b\tBind local end to specified address\n\
-l\tListening mode\n\
-d\tDaemon mode (listen only) -- process multiple connections\n\
-e\tTerminate as soon as EOF is seen in any direction\n\
-n\tAvoid reverse DNS lookups\n\
", my_name);
  exit(1);
}

static void
mdie(char *x, ...)
{
  va_list args;
  char *e = strerror(errno);

  va_start(args, x);
  fprintf(stderr, "%s: ", my_name);
  vfprintf(stderr, x, args);
  fprintf(stderr, ": %s\n", e);
  exit(1);
}

static void
die(char *x, ...)
{
  va_list args;

  va_start(args, x);
  fprintf(stderr, "%s: ", my_name);
  vfprintf(stderr, x, args);
  fputc('\n', stderr);
  exit(1);
}

static void *
xmalloc(int size)
{
  void *x = malloc(size);
  if (!x)
    die("Out of memory");
  return x;
}

static struct sockaddr *
alloc_sockaddr(void)
{
  struct sockaddr *sa;
  sock_addr_length = (addr == ux) ? sizeof(struct sockaddr_un) : sizeof(struct sockaddr_in);
  sa = xmalloc(sock_addr_length);
  memset(sa, 0, sizeof(*sa));
  sa->sa_family = (addr == ux) ? AF_UNIX : AF_INET;
  return sa;
}

static char *
parse_addr(struct sockaddr **sap, char *a, int need_port, int need_addr)
{
  char *o = a;
  struct sockaddr *sa = alloc_sockaddr();

  switch (addr)
    {
    case ip:
      {
	struct sockaddr_in *s = (struct sockaddr_in *) sa;
	char *p = strchr(a, ':');

	if (p)
	  *p++ = 0;
	else if (need_port)
	  die("%s: port number required", a);
	s->sin_family = AF_INET;
	if (!*a && need_addr)
	  a = "localhost";
	if (*a)
	  {
	    struct hostent *e = gethostbyname(a);
	    if (!e)
	      mdie(a);
	    if (e->h_addrtype != AF_INET && e->h_length != sizeof(struct in_addr))
	      die("%s: invalid address type", a);
	    memcpy(&s->sin_addr, e->h_addr, sizeof(struct in_addr));
	    o = e->h_name;
	  }
	if (p)
	  {
	    struct servent *e = NULL;

	    if (proto != udp)
	      {
		e = getservbyname(p, "tcp");
		if (e)
		  proto = tcp;
	      }
	    else if (!e && proto != tcp)
	      {
		e = getservbyname(p, "udp");
		if (e)
		  proto = udp;
	      }
	    if (!e)
	      {
		char *z;
		long int i = strtol(p, &z, 10);
		if (i <= 0 || i > 0xffff || (z && *z))
		  die("%s: invalid port", p);
		s->sin_port = htons(i);
	      }
	    else
	      s->sin_port = e->s_port;
	    p[-1] = ':';
	  }
	break;
      }
    case ux:
      {
	struct sockaddr_un *s = (struct sockaddr_un *) sa;
	if (strlen(a) >= sizeof(s->sun_path))
	  die("%s: address too long", a);
	s->sun_family = AF_UNIX;
	strcpy(s->sun_path, a);
	break;
      }
    }
  *sap = sa;
  return o;
}

static char *
name_addr(struct sockaddr *sa)
{
  switch (addr)
    {
    case ip:
      {
	struct sockaddr_in *a = (struct sockaddr_in *) sa;
	struct hostent *h = avoid_dns_p ? NULL : gethostbyaddr((char *) &a->sin_addr.s_addr, sizeof(struct in_addr), AF_INET);
	if (h)
	  return h->h_name;
	else
	  return inet_ntoa(a->sin_addr);
      }
    case ux:
      {
	struct sockaddr_un *a = (struct sockaddr_un *) sa;
	return a->sun_path;
      }
    default:
      return "?";
    }
}

static RETSIGTYPE
sigchld_handler(int sig)
{
  while (waitpid(-1, NULL, WNOHANG) > 0)
    ;
}

static void
setup_sigchld(void)
{
  struct sigaction sa;

  memset(&sa, 0, sizeof(sa));
  sa.sa_handler = sigchld_handler;
  sa.sa_flags = SA_RESTART;
  if (sigaction(SIGCHLD, &sa, NULL) < 0)
    mdie("sigaction");
}

static void
gw(int sk)
{
  if (cmd)
    {
      char *sh = getenv("SHELL");
      if (!sh)
	sh = "/bin/sh";
      close(0);
      close(1);
      if (dup(sk) != 0 || dup(sk) != 1)
	mdie("dup");
      close(sk);
      execl(sh, sh, "-c", cmd, NULL);
      mdie("exec");
    }
  else
    {
      char ib[4096], ob[4096];
      char *ibr = ib;
      char *ibw = ib;
      char *ibe = ib + sizeof(ib);
      char *obr = ob;
      char *obw = ob;
      char *obe = ob + sizeof(ob);
      int ieof = 0;
      int oeof = 0;
      int n;
      fd_set in, out;

      if (proto == udp)
	{
	  if (listen_p)
	    ieof = 1;
	  else
	    oeof = 1;
	}
      if (!ieof && fcntl(0, F_SETFL, O_NDELAY) < 0 ||
	  !oeof && fcntl(1, F_SETFL, O_NDELAY) < 0 ||
	  fcntl(sk, F_SETFL, O_NDELAY) < 0)
	mdie("fcntl");
      FD_ZERO(&in);
      FD_ZERO(&out);
      for(;;)
	{
	  if (ibr < ibe && !ieof)
	    FD_SET(0, &in);
	  if (ibw < ibr)
	    FD_SET(sk, &out);
	  if (obr < obe && !oeof)
	    FD_SET(sk, &in);
	  if (obw < obr)
	    FD_SET(1, &out);
	  if (ibr == ib && ieof == 1)
	    {
	      shutdown(sk, 1);
	      ieof = 2;
	    }
	  if (ibr == ib && obr == ob &&
	      (single_eof_p ? (ieof || oeof) : (ieof && oeof)))
	    break;
	  if (select(sk+1, &in, &out, NULL, NULL) < 0)
	    mdie("select");
	  if (FD_ISSET(sk, &out))
	    {
	      FD_CLR(sk, &out);
	      n = write(sk, ibw, ibr - ibw);
	      if (n < 0 && errno != EAGAIN && errno != EINTR)
		mdie("socket write");
	      ibw += n;
	      if (ibr == ibw)
		ibr = ibw = ib;
	    }
	  if (FD_ISSET(1, &out))
	    {
	      FD_CLR(1, &out);
	      n = write(1, obw, obr - obw);
	      if (n < 0 && errno != EAGAIN && errno != EINTR)
		mdie("stdio write");
	      obw += n;
	      if (obr == obw)
		obr = obw = ob;
	    }
	  if (FD_ISSET(0, &in))
	    {
	      FD_CLR(0, &in);
	      n = read(0, ibr, ibe - ibr);
	      if (n < 0 && errno != EAGAIN && errno != EINTR)
		mdie("stdio read");
	      if (n)
		ibr += n;
	      else
		ieof = 1;
	    }
	  if (FD_ISSET(sk, &in))
	    {
	      FD_CLR(sk, &in);
	      n = read(sk, obr, obe - obr);
	      if (n < 0 && errno != EAGAIN && errno != EINTR)
		mdie("socket read");
	      if (n)
		obr += n;
	      else
		oeof = 1;
	    }
	}
    }
}

int
main(int argc, char **argv)
{
  int c, sk;

  if (argc == 2 && !strcmp(argv[1], "--version"))
    {
      puts("This is sock " SOCK_VERSION ". Be happy.");
      return 0;
    }

  my_name = argv[0];
  while ((c = getopt(argc, argv, opts)) > 0)
    switch (c)
      {
      case 'v':
	verbose++;
	break;
      case 'd':
	daemon_p++;
	break;
      case 't':
	if (proto != deflt)
	  usage();
	proto = tcp;
	break;
      case 'u':
	if (proto != deflt)
	  usage();
	proto = udp;
	break;
      case 'x':
	if (proto != deflt)
	  usage();
	proto = uxp;
	break;
      case 'l':
	listen_p++;
	break;
      case 'b':
	if (na_bind)
	  usage();
	na_bind = optarg;
	break;
      case 'e':
	single_eof_p++;
	break;
      case 'n':
	avoid_dns_p++;
	break;
      default:
	usage();
      }

  if (argc == optind + 2)
    cmd = argv[optind+1];
  else if (argc != optind + 1)
    usage();

  if (proto == uxp)
    addr = ux;

  if (listen_p)
    {
      if (na_bind)
	usage();
      na_bind = parse_addr(&sa_bind, argv[optind], 1, 0);
    }
  else
    {
      if (na_bind)
	na_bind = parse_addr(&sa_bind, na_bind, 0, 0);
      na_conn = parse_addr(&sa_conn, argv[optind], 1, 1);
    }

  switch (proto)
    {
    case deflt:
    case tcp:
      sk = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
      break;
    case udp:
      sk = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
      break;
    case uxp:
      sk = socket(PF_UNIX, SOCK_STREAM, 0);
      break;
    default:
      sk = -1;
    }
  if (sk < 0)
    mdie("socket");
  if (sa_bind)
    {
      int one = 1;
      if (setsockopt(sk, SOL_SOCKET, SO_REUSEADDR, (void *) &one, sizeof(one)) < 0)
	mdie("setsockopt(SO_REUSEADDR)");
      if (bind(sk, sa_bind, sock_addr_length) < 0)
	mdie("bind");
    }

  if (listen_p)
    {
      struct sockaddr *sa_incoming;
      if (verbose)
	fprintf(stderr, "Listening on %s\n", na_bind);
      if (proto == udp)
	{
	  gw(sk);
	  return 0;
	}
      if (listen(sk, (daemon_p ? 10 : 1)) < 0)
	mdie("listen");
      if (cmd && daemon_p)
	setup_sigchld();
      sa_incoming = alloc_sockaddr();
      for(;;)
	{
	  int l = sock_addr_length;
	  int ns = accept(sk, sa_incoming, &l);
	  if (ns < 0)
	    mdie("accept");
	  if (verbose)
	    fprintf(stderr, "Got connection from %s\n", name_addr(sa_incoming));
	  if (!daemon_p)
	    {
	      close(sk);
	      gw(ns);
	      return 0;
	    }
	  if (cmd)
	    {
	      pid_t p = fork();
	      if (p < 0)
		mdie("fork");
	      if (!p)
		{
		  close(sk);
		  gw(ns);
		  exit(0);
		}
	    }
	  else
	    gw(ns);
	  close(ns);
	}
    }
  else
    {
      if (verbose)
	fprintf(stderr, "Connecting to %s\n", na_conn);
      if (connect(sk, sa_conn, sock_addr_length) < 0)
	mdie("connect");
      gw(sk);
    }

  return 0;
}
