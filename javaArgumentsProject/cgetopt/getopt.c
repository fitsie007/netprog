#include <stdio.h>
#include <unistd.h>

int main(int argc, char *argv[])
{

	int n;
	while((n = getopt(argc, argv, "abc:r:")) != -1)
	{
		switch(n)
		{
			case 'a':
                printf("—A\n");
                break;

			case 'b':
                printf("—B\n");
                break;

			case 'c':
                printf("—C : %s\n", optarg);
                break;
                
            case 'r':
                printf("—r : %s\n", optarg);
                break;

			default:
                printf("Wrong usage\n");
                break;
		}
	}

	return 0;
}