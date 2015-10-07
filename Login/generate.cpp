#include <iostream>
#include <stdlib.h>
#include <algorithm>
#include <fstream>
using namespace std;
int main(int argc, char* argv[]) {
	int cnt;
	if (argc != 2) {
		cnt = 300;
	} else {
		cnt = atoi(argv[1]);
	}
	int a[cnt],b[cnt];
	for (int i=0;i<cnt;i++) {
		a[i] = 10203 + i;
		b[i] = 48294 + i;
	}
	random_shuffle(a,a+cnt);
	random_shuffle(b,b+cnt);
	ofstream f("loginid.csv");
	for (int i=0;i<cnt;i++) {
		f<<"cs"<<a[i]<<","<<"pass"<<b[i]<<endl;
	}
	f.close();
	return 0;
}
