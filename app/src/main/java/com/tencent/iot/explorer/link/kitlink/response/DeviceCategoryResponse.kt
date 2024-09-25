package com.tencent.iot.explorer.link.kitlink.response

data class DeviceCategoryResponse(
    val Infos: List<FirstLevelCategoryInfo> = arrayListOf(),
    val RequestId: String = "",
    val Total: Int = 0,
)

data class FirstLevelCategoryInfo(
    val CategoryKey: String = "",
    val CategoryName: String = "",
    val CategoryEnName: String = "",
)

data class SecondDeviceCategoryResponse(
    val Infos: List<SecondLevelCategoryInfo> = arrayListOf(),
    val RequestId: String = "",
    val Total: Int = 0,
)

data class SecondLevelCategoryInfo(
    val CategoryKey: String = "",
    val SubCategoryKey: String = "",
    val SubCategoryName: String = "",
    val SubCategoryEnName: String = "",
)

data class CategoryDeviceResponse(
    val Infos: List<CategoryDeviceInfo> = arrayListOf(),
    val RequestId: String = "",
    val Total: Int = 0,
)

data class CategoryDeviceInfo(
    val Id: Int = 0,
    val SubCategoryKey: String = "",
    val IsRelatedProduct: Boolean = false,
    val ProductId: String = "",
    val ProductName: String = "",
    val ProductUrl: String = "",
    val IsRecommend: Int = 0,
    val Type: String = "",
    val ExtraInfo: String = "",
)