# CalcDesk Android — Google Play 上架清单

项目路径：`AndroidWorkspace/CalcDesk`  
应用 ID：`com.qiyuan.calcdesk`  
最低系统：Android 8.0 (API 26)  
目标：API 36  

## 产品定位（商店文案建议）

**英文短描述（≤80 字符）**  
Free offline calculators: units, dates, BMI, mortgage, JSON & more.

**英文完整描述要点**  
- 34 tools: date/time, unit conversion, math, developer, life  
- 100% on-device — no account, no ads in v1.0  
- Privacy-first: no INTERNET permission  
- Health/finance tools include clear “reference only” disclaimers  

**中文短描述**  
免费离线计算器：单位换算、日期、BMI、房贷、JSON 等。

## Google Play 政策 / Data Safety

| 项 | 填写建议 |
|----|----------|
| 收集的数据 | **不收集** |
| 是否加密传输 | N/A（无网络） |
| 是否可申请删除 | N/A |
| 权限 | **无敏感权限**（无 INTERNET / 定位 / 通讯录 / 相机） |
| 广告 | 本版本无广告；若日后接入须更新 Data Safety 与隐私政策 |
| 面向儿童 | 非「专为儿童」；可选适合全年龄 Tools |
| 健康声明 | BMI / 预产期页内免责声明；商店说明「非医疗建议」 |
| 金融声明 | 房贷 / 复利页内免责声明；「非投资建议」 |

应用内已提供：隐私政策、关于、联系邮箱 `gudongdong8@gmail.com`。  
网页政策：https://calcdesk.app/privacy  

## 构建与签名

```bat
cd C:\Users\GDD\Documents\androidProject\AndroidWorkspace\CalcDesk
gradlew.bat :app:assembleDebug
gradlew.bat :app:bundleRelease
```

上架请用 **upload key** 签名 AAB（`bundleRelease`），在 Play Console 创建应用后上传。  
勿把 `.jks` / 密码提交进 Git。

## 上架前自检

- [ ] 用真机点一遍全部 34 个工具  
- [ ] 中英文系统语言下标题/描述正常  
- [ ] 隐私政策页与商店 Data Safety 一致  
- [ ] 准备 2–8 张手机截图（首页 + 单位 + 计算器 + 生活工具）  
- [ ] Feature graphic 1024×500  
- [ ] 高分辨率图标 512×512（可基于 `ic_launcher_foreground`）  
- [ ] 内容分级问卷（通常 Tools / Utility）  
- [ ] 目标受众与「未专为儿童设计」勾选准确  

## 架构说明

- Kotlin + Jetpack Compose + Material 3  
- Navigation Compose 带 fade/slide 转场  
- 全部计算本地完成，无追踪 SDK  
