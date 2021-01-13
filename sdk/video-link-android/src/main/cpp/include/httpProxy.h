#ifndef __HTTPPROXY_H_
#define __HTTPPROXY_H_
//#ifdef __cplusplus
//extern "C" {
//#endif

#include <stddef.h>
#include <stdint.h>
#include <string>

__attribute__ ((visibility ("default")))
int proxy_init(std::string peername, uint16_t *port);

__attribute__ ((visibility ("default")))
void proxy_deinit();

//#ifdef __cplusplus
//}
//#endif
#endif
