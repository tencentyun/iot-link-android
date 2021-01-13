#ifndef __APPWRAPPER_H_
#define __APPWRAPPER_H_
//#ifdef __cplusplus
//extern "C" {
//#endif

#include <stddef.h>
#include <stdint.h>
#include <string>

__attribute__ ((visibility ("default")))
int startServiceWithPeername(std::string peername);

__attribute__ ((visibility ("default")))
std::string delegateHttpFlv();

__attribute__ ((visibility ("default")))
int runSendService();

__attribute__ ((visibility ("default")))
int setQcloudApiCred(const char *sec_id, const char *sec_key);

__attribute__ ((visibility ("default")))
int setDeviceInfo(const char *pro_id, const char *dev_name);

__attribute__ ((visibility ("default")))
int dataSend(uint8_t *data, size_t len);

__attribute__ ((visibility ("default")))
void stopService();

//#ifdef __cplusplus
//}
//#endif
#endif