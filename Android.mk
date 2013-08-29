#ifeq ($(TYS_APP_PHONE_NUMBER_LOCARION_SUPPORT),true)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#xuwen 2010-12-03 add for optional building start
LOCAL_MODULE_TAGS := optional
#xuwen 2010-12-03 add end

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := PhoneNumLocation

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
