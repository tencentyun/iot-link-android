package com.tencent.iot.explorer.link.util.picture.imageselectorbrowser;

public class ImageSelectorConstant {
	/** 最大图片选择次数，int类型，默认9 */
	public static final String EXTRA_SELECT_COUNT = "max_select_count";

	/** 图片选择模式，默认多选 */
	public static final String EXTRA_SELECT_MODE = "select_count_mode";

	/** 是否显示相机，默认显示 */
	public static final String EXTRA_SHOW_CAMERA = "show_camera";

	/** 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合 */
	public static final String EXTRA_RESULT = "select_result";

	/** 选择结果，返回为 BITMAP byte[]; 图片路径集合 */
	public static final String EXTRA_RESULT_BITMAP = "select_result_bitmap";

	/** 默认选择集 */
	public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

	/** result参数 */
	public static final int REQUEST_IMAGE = 2001;
	public static final int REQUEST_IMAGE_MIANTAIN = 1001;
	public static final int REQUEST_IMAGE_CHECK = 1002;
	/**最大图片选择次数的默认值*/
	public static final int MAX_SELECT_COUNT = 9;
	
	/**选择模式的默认值*/
	public static final int DEFAULT_SELECT_MODE = 2;

	/** 裁切图，宽度 */
	public static final String EXTRA_CLIP_WIDTH = "clip_width";
	/** 裁切图，高度 */
	public static final String EXTRA_CLIP_HEIGHT = "clip_height";
}
