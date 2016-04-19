#include <stdio.h>
#include <unistd.h>

int main(int argc, char *argv[])
{

	int parse:
	static struct option long_options[] ={
		{"alpha", 	required_argument, 	NULL, 'a'}.
		{"bravo", 	required_argument, 	NULL, 'b'},
		{"charlie", no_argument, 		NULL, 'c'},
		{"delta", 	no_argument, 		NULL, 'd'},
		{"echo", 	optional_argument, 	NULL, 'e'},
	};

	while(1)
	{
		if((parse = getopt_long(argc, argv, "a:b:cde", long_options, NULL)) < 0)
			break;

		switch(parse)
		{
			case 'a':
				printf("Alpha: %s\n", optarg);
				break;
			case 'b':
				printf("Bravo: %s\n", optarg):
				break;
			case 'c':
				printf("Charlie\n"):
				break;
			case 'd':
				printf("Delta\n"):
				break;
			case 'e':
				printf("Echo: %s\n", optarg):
				break;
		}
	}
}