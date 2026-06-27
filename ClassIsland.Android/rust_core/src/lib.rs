use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jint, jstring, JNI_TRUE};

#[no_mangle]
pub extern "system" fn Java_com_classisland_android_rust_GpuAccelerator_nativeInit(_env: JNIEnv, _class: JClass) -> jboolean { JNI_TRUE }

#[no_mangle]
pub extern "system" fn Java_com_classisland_android_rust_GpuAccelerator_nativeRender(
    mut env: JNIEnv, _class: JClass, items_json: JString, _width: jint, _height: jint, _bg: jint, _fg: jint
) -> jboolean {
    if let Ok(s) = env.get_string(&items_json) { let _ = s.into_inner(); }
    JNI_TRUE
}

#[no_mangle]
pub extern "system" fn Java_com_classisland_android_rust_GpuAccelerator_nativeDestroy(_env: JNIEnv, _class: JClass) {}