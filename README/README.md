# JitsiVideoChat

Android-приложение для автоматического запуска видеоконференции Jitsi  
с поддержкой **ручного обновления APK без Google Play**.

---

## 🚀 Назначение приложения

Приложение выполняет следующие задачи:

1. При запуске проверяет наличие новой версии
2. Если обновление доступно — предлагает установить APK
3. Если обновления нет — сразу запускает видеоконференцию Jitsi
4. Обновление происходит напрямую через GitHub Releases

---

## 🧱 Архитектура проекта

```text
com.example.jitsiapp
│
├── MainActivity.java
│   └── Точка входа, UI, навигация
│
├── jitsi
│   └── JitsiLauncher.java
│       └── Запуск видеоконференции Jitsi
│
├── update
│   ├── UpdateChecker.java
│   │   └── Проверка версии (version.json)
│   │
│   └── ApkDownloader.java
│       └── Загрузка и установка APK
│
└── res
    └── layout
        └── activity_update.xml
            └── Экран загрузки обновления
```

## 🔄 Поток работы приложения

```text
[Запуск приложения]
        |
        v
[MainActivity.onCreate]
        |
        v
[UpdateChecker.check()]
        |
        +--> Есть обновление?
        |        |
        |        +--> Да
        |        |     |
        |        |     v
        |        | [Диалог обновления]
        |        |     |
        |        |     v
        |        | [Загрузка APK]
        |        |     |
        |        |     v
        |        | [Установка APK]
        |        |
        |        +--> Нет
        |              |
        |              v
        +--------> [Запуск Jitsi]
```

## 🔢 Версионирование

versionCode — обязательно увеличивается
versionName — соответствует Git-тегу (v1.1.5)
Обновление предлагается только если:
```text
remote versionCode > local versionCode
```

## 📦 Механизм обновления

Источник информации об обновлении:

https://raw.githubusercontent.com/sSergey8/jitsi-update/main/version.json


Пример version.json:
```text
{
"versionCode": 9,
"versionName": "1.1.9",
"apkUrl": "https://ssergey8.github.io/jitsi-update/download.html"
}
```

apkUrl — постоянная ссылка, всегда ведущая на актуальную версию APK.


## 🌐 Распространение APK
APK хранятся в репозитории jitsi-release<br>
Каждый релиз публикуется через GitHub Releases<br>
Пользователям выдаётся одна постоянная ссылка<br>
Перенаправление реализовано через GitHub Pages (download.html)

## 🔐 Установка из неизвестных источников
Приложение не использует Google Play<br>
Пользователь разрешает установку из неизвестных источников<br>
После возврата из настроек установка продолжается автоматически

## 🎥 Jitsi
SDK: org.jitsi.react:jitsi-meet-sdk<br>
Сервер: https://meet.jit.si<br>
Комната: фиксированная<br>
Prejoin / Welcome Page: отключены

## ⚙ CI / CD
Push тега vX.Y.Z<br>
Сборка assembleRelease<br>
Публикация APK в jitsi-release<br>
Обновление version.json в jitsi-update

## 🛠 Технологии
Java<br>
Android SDK<br>
GitHub Actions<br>
GitHub Releases<br>
GitHub Pages<br>
Jitsi Meet SDK

## 📌 Примечания
Обновление полностью автономное<br>
Архитектура рассчитана на простоту и надёжность
