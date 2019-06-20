Android.mk
一、变量说明：

1.LOCAL_PATH:= $(call my-dir)

此行代码在Android.mk的开头，用于给出当前文件的路径

 LOCAL_PATH 用于在开发树中查找源文件

 宏函数’my-dir’, 由编译系统提供，用于返回当前路径（即包含Android.mk file文件的目录）

 

2.LOCAL_PACKAGE_NAME := SecSettings 或 LOCAL_MODULE:= SecSettings

标识在Android.mk文件中描述的每个模块。名称必须是唯一的且不包含空格。

注意编译系统会自动产生合适的前缀和后缀：

静态库：又称为文档文件（Archive File），多个.o文件的集合，linux中静态库文件的后缀为“.a”

LOCAL_STATIC_JAVA_LIBRARIES := static-library

LOCAL_STATIC_JAVA_LIBRARIES += libSR

共享库：多个.o文件的集合，一个被命名为'foo'的共享库模将会生成'libfoo.so'文件。

        LOCAL_SHARED_LIBRARIES := libBMapApiEngine_v1_3_5

 重要注意事项：如果你把库命名为‘libhelloworld’，编译系统将不会添加任何的lib前缀，也会生成libhelloworld.so，这是为了支持来源于Android平台的源代码的Android.mk文件。如果你确实需要这么做的话。

 

3.LOCAL_MODULE_TAGS := optional / user / eng / tests  可选定义

 该模块在所有版本下都编译/ 该模块只在user版本下才编译/ 该模块只在eng版本下才编译/ 该模块只在tests版本下才编译

 

 4.LOCAL_OVERRIDES_PACKAGES := Settings

 覆盖其他所有同名的应用

 

5.LOCAL_CERTIFICATE := platform 可选定义

编译一个需要platform签名的APK，而不是share编译

 

6.LOCAL_PROGUARD_FLAG_FILES := proguard.flags

指定不需要混淆的native方法与变量的proguard.flags文件

ProGuard的主要作用就是混淆：Java的字节码一般是非常容易反编译的。为了很好的保护Java源代码，我们往往会对编译好的class文件进行混淆处理。

 

7.LOCAL_PROGUARD_ENABLED:= disabled

制定编译的工程，不要使用代码混淆的工具进行代码混淆：

 

8.LOCAL_CLASSPATH := $(LOCAL_PATH)/lib/maps.jar

 

9.LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += src/com/android/settings/nearby/IMediaServer.aidl

变量 LOCAL_SRC_FILES 必须包含将要编译打包进模块中的源代码文件

不用在这里列出头文件和包含文件，因为编译系统将会自动为你找出依赖型的文件；仅仅列出直接传递给编译器的源代码文件就好

 

10.LOCAL_RESOURCE_OVERLAY_DIR := $(LOCAL_PATH)/TN_CHN_OPEN/res

指定资源文件路径

11.LOCAL_AAPT_FLAGS := $(SEC_DEV_APP_LOCAL_AAPT_FLAGS)

指定打包资源文件

因为Android的工具aapt在生成apk文件时默认地会编译并压缩res/下的文件，而一些系统文件则不需要被压缩（否则在读取该文件时需要解压缩），在Android.mk文件需要指定以下选项告诉aapt工具不压缩所的文件。

例如Android.mk文件需要指定以下选项告诉aapt工具不压缩所有.dat文件：

LOCAL_AAPT_FLAGS := -0 .dat

 

二、语句解释

1.include $(CLEAR_VARS)

CLEAR_VARS由编译系统提供(可以在 android 安装目录下的/build/core/config.mk 文件看到其定义，为 CLEAR_VARS:=$(BUILD_SYSTEM)/clear_vars.mk)，因为所有的编译控制文件都在同一个GNU MAKE执行环境中，所有的变量都是全局的，让GNU MAKEFILE清除许多LOCAL_XXX变量，例如 :LOCAL_MODULE, LOCAL_SRC_FILES, LOCAL_STATIC_LIBRARIES,  等等...

该语句的意思就是把CLEAR_VARS变量所指向的脚本文件包含进来。

 

2. include $(BUILD_PACKAGE)

指定编译生成APK

3.include $(BUILD_STATIC_LIBRARY)

用于编译一个静态库，将会生成一个名为lib$(LOCAL_MODULE).a的文件

静态库不会复制到的APK包中，但是能够用于编译共享库。

4.include $(BUILD_SHARED_LIBRARY)

 指向编译脚本，根据所有的在 LOCAL_XXX 变量把列出的源代码文件编译成一个共享库，将生成一个名为lib$(LOCAL_MODULE).so的文件

注意：你必须至少在包含这个文件之前定义LOCAL_MODULE和LOCAL_SRC_FILES

 

5.$(info RES_OVERLAY TN_CHN_OPEN/RES/)

相当于代码中的log信息

 

6.ifneq($(filter santos10%,$(TARGET_PRODUCT)),)

##############################################

endif

判断是否含有santos10***的string，有的话，进入ifneq条件

Filter有两个参数

 

7..ifneq($(filter %wifi,$(PROJECT_NAME))，)

###############################################

endif

判断是否含有***wifi，如果有进入ifneq条件

8.  +=

原来有的话不覆盖

9.：=

之前的值清空，重新复制

 

10.LOCAL_RESOURCE_OVERLAY_DIR:=$(LOCAL_PAHT)/TN_CHN_OPEN/res $(LOCAL_RESOURCE_OVERLAY_DIR)

相当于

LOCAL_RESOURCE_OVERLAY_DIR  ：=  $(LOCAL_PAHT)/TN_CHN_OPEN/res

LOCAL_RESOURCE_OVERLAY_DIR  +=  LOCAL_RESOURCE_OVERLAY_DIR(原来的)

 

11.ifeq($(findstring santos3g,$(PROJECT_NAME))，cantos3g)

###################################################

endif

如果PROJECT_NAME中含有santos3g，进入ifeq条件

 

 

 如何查找PROJECT_NAME与TARGET_PRODUCT的值

1.到编译log中搜索，即可得到

2.到脚本中查找，

./buildscript/build中

export PROJECT_NAME=${_BUILD_PROJECT_NAME%%_*}

而_BUILD_PROJECT_NAME就是$1

我们的输入如果是santos103g_chn_open那么PROJECT_NAME=santos103g

12. ifeq (true,$(call spf_check, EC_PRODUCT_FEATURE_TEMP_REGION,CHN))

Check 地区是不是CHN，如果是，进入ifeq条件

三、Settings的Android.mk 的相关介绍

1.地区宏：SEC_PRODUCT_FEATURE_TEMP_REGION 

ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_TEMP_REGION,HKTW))

ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_TEMP_REGION,CHN))

2.运营商宏：SEC_PRODUCT_FEATURE_TEMP_OPERATOR

ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_TEMP_OPERATOR,CMCC))

ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_TEMP_OPERATOR,CTC))

3.双卡宏：BUILD_MULTISIM_PROJECT

        SEC_PRODUCT_FEATURE_COMMON_DSDS_SUPPORT

ifeq ($(BUILD_MULTISIM_PROJECT),true)

控制双卡相关文件路径为：TN_MultiSIM/…

ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_COMMON_DSDS_SUPPORT,TRUE))

控制双卡相关文件路径为：TN_DSDS/…

 

上面三个宏的定义路径：

//JBP_MAIN/Maple/JBP98x/model/vendor/wilcoxds/SecProductFeature.wilcoxdszn

OpenGrok/android/vendor/samsung/wilcoxds/SecProductFeature.wilcoxdszn

# Region, Operator feature

SEC_PRODUCT_FEATURE_TEMP_REGION="CHN"

SEC_PRODUCT_FEATURE_TEMP_OPERATOR="CU"

#Common DSDS Feature

SEC_PRODUCT_FEATURE_COMMON_DSDS_SUPPORT=TRUE

# another micro for multisim module

SEC_PRODUCT_FEATURE_COMMON_USE_MULTISIM=TRUE

 

4.平台控制宏： BUILD_RIL_MARVELL_RIL

ifeq ($(BUILD_RIL_MARVELL_RIL), true)

5.项目名控制：TARGET_PRODUCT

ifneq ($(filter wilcoxds%, $(TARGET_PRODUCT)),)