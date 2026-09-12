# TPlus_JFFZ 手机端重构项目

## 项目概述

基于原 PDA 进销存 APP（包名 `com.example.tplus_jffz`）重构的 Android 手机端项目，保留原有 7 个 Activity 和 21 个 REST API 端点，将 PDA 工业影像引擎扫码替换为手机摄像头扫码（ML Kit + CameraX）。

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 34 (Android 14)
- **UI**: AndroidX + ConstraintLayout + Material Components
- **扫码**: Google ML Kit Barcode Scanning + CameraX
- **网络**: Retrofit 2.9 + OkHttp + Gson
- **权限**: AndroidX ActivityCompat 运行时权限
- **离线缓存**: Room 2.6 + SQLite (条码查物料支持离线优先)

## 导入方式

1. 下载并解压 `TPlus_JFFZ.zip`
2. Android Studio → File → New → Import Project → 选择解压后的 `TPlus_JFFZ` 文件夹
3. 等待 Gradle Sync 完成（会自动下载依赖）
4. 连接 Android 设备或启动模拟器（API 24+）
5. 点击 Run

## 关键配置

### 服务器地址
- 在 `LinkActivity` 中配置 T+ 服务端地址
- 默认地址: `http://192.168.1.100:8080`
- 若服务端仅支持 HTTP，已配置 `android:usesCleartextTraffic="true"`

### 扫码音效
- `app/src/main/res/raw/` 下放置 `succeed.mp3`（扫描成功）和 `errar.mp3`（扫描失败）
- 当前为空占位文件，需替换为真实音效

## 项目结构

```
app/src/main/java/com/example/tplus_jffz/
├── api/
│   ├── RetrofitClient.kt      # OkHttp + Retrofit 客户端
│   └── TPlusApi.kt            # 22 个 REST API 接口定义
├── data/
│   ├── db/
│   │   ├── MaterialCacheEntity.kt   # Room 缓存实体
│   │   ├── MaterialDao.kt           # Room DAO
│   │   └── AppDatabase.kt           # Room 数据库
│   └── model/
│       ├── User.kt                # 登录相关数据类
│       └── ApiModels.kt           # 全部业务数据类（DTO）
├── ui/
│   ├── login/LoginActivity.kt
│   ├── link/LinkActivity.kt
│   ├── index/IndexActivity.kt + MenuAdapter.kt
│   ├── updatepwd/UpdatePwdActivity.kt
│   ├── saledelivery/SaleDeliveryActivity.kt + SaleDetailAdapter.kt
│   ├── rdrecord/RDRecordActivity.kt + RDRecordDetailAdapter.kt
│   ├── transvoucher/TransVoucherActivity.kt + TransVoucherDetailAdapter.kt
│   └── scan/ScanActivity.kt   # ML Kit 扫码
├── utils/
│   ├── PermissionsManager.kt      # 运行时权限工具
│   └── BarcodeMaterialHelper.kt # 条码查物料工具（离线优先：Room + /getmaterialbycode）
└── res/layout/                    # 8 个 Activity 布局 + 2 个列表项布局
```

## 后续改造建议

1. **服务端验证**: 先用现有 T+ 环境测试 22 个 API 端点是否仍可用（含 `/getmaterialbycode`）
2. **离线模式**: 当前为纯在线，如需断网可用可引入 Room + WorkManager
3. **图片上传**: `/upload` 端点待接入实际文件上传逻辑
