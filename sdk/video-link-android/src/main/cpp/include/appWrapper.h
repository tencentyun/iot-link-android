#ifndef __APPWRAPPER_H_
#define __APPWRAPPER_H_
//#ifdef __cplusplus
//extern "C" {
//#endif
#define _GLIBCXX_USE_CXX11_ABI 0

#include <stddef.h>
#include <stdint.h>

int startServiceWithPeername(std::string peername);
std::string delegateHttpFlv();
void stopService();

//#ifdef __cplusplus
//}
//#endif
#endif