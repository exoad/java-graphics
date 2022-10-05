#include <fstream>

int main()
{
	ofstream f("shades_.txt");
	for(int i=5;i<=255;i+=25) f<<i<<",\n";
}
