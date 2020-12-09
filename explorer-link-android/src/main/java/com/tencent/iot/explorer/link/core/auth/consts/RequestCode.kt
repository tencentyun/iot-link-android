package com.tencent.iot.explorer.link.core.auth.consts

object RequestCode {

    /*************用户接口开始**************/
    const val phone_login = 1000
    const val email_login = 1001
    const val wechat_login = 1002
    const val send_phone_code = 1003
    const val send_email_code = 1004
    const val check_phone_code = 1005
    const val check_email_code = 1006
    const val phone_register = 1007
    const val email_register = 1008
    const val bind_phone = 1009
    const val email_reset_pwd = 1010
    const val phone_reset_pwd = 1011
    const val bind_xg = 1012
    const val unbind_xg = 1013
    const val feedback = 1014
    const val user_info = 1015
    const val message_list = 1016
    const val delete_message = 1017
    const val reset_password = 1018
    const val logout = 1019
    const val user_setting = 1020
    const val update_user_setting = 1021
    const val modify_alias = 1022
    const val find_phone_user = 1023
    const val find_email_user = 1024
    const val modify_portrait = 1025

    /*************用户接口结束**************/

    /*************家庭接口开始**************/
    const val family_list = 2000
    const val create_family = 2001
    const val room_list = 2002
    const val create_room = 2003
    const val modify_family = 2004
    const val modify_room = 2005
    const val delete_family = 2006
    const val delete_room = 2007
    const val family_info = 2008
    const val send_family_invite = 2009
    const val delete_family_member = 2010
    const val join_family = 2011
    const val exit_family = 2012
    const val member_list = 2013

    /*************家庭接口结束**************/

    /*************设备接口开始**************/
    const val device_list = 3000
    const val device_online_status = 3001
    const val scan_bind_device = 3002
    const val modify_device_alias_name = 3003
    const val wifi_bind_device = 3004
    const val delete_device = 3005
    //设备状态
    const val device_data = 3006
    //设备详情
    const val get_device_info = 3007
    const val control_device = 3008
    const val control_panel = 3009
    const val device_product = 3010
    const val change_room = 3011
    const val get_bind_device_token = 3012
    const val check_device_bind_token_state = 3013
    const val trtc_call_device = 3015

    /*************设备接口结束**************/

    /*************云端定时接口开始**************/
    const val time_list = 4000
    const val create_timer = 4001
    const val modify_timer = 4002
    const val modify_timer_status = 4003  //打开或关闭
    const val delete_timer = 4004

    /*************云端定时接口结束**************/

    /*************设备分享接口开始**************/
    const val share_device_list = 6000
    const val share_user_list = 6001
    const val delete_share_device = 6002
    const val delete_share_user = 6003
    const val send_share_invite = 6004
    const val bind_share_device = 6008

    /*************设备分享接口结束**************/
}