package com.tencent.iot.explorer.link.core.demo;

import android.content.Intent;
import android.widget.Button;

import com.tencent.iot.explorer.link.demo.core.activity.LoginActivity;
import com.tencent.iot.explorer.link.demo.ModuleActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29)
public class ModuleActivityTest {
    @Test
    public void testJump2Login() {
        ModuleActivity activity = Robolectric.buildActivity(ModuleActivity.class).create().get();
        Button btn = activity.findViewById(R.id.btn_1);
        btn.performClick();
        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
