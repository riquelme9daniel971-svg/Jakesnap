# Jakesnap

App Android simples para baixar vídeos do YouTube em 720p MP4 via menu **Compartilhar**.

## Características

- Aparece no menu Compartilhar do Android
- Download em 720p MP4 com máxima velocidade (concurrent-fragments 16, player_client android/web, etc.)
- Usa youtubedl-android + FFmpeg
- UI amarela com progresso em tempo real
- Salva em `Movies/Jakesnap`
- Botão para abrir no player externo (Lark Player ou padrão)

## Como compilar

### Opção 1 — GitHub Actions (recomendado)

1. Crie um repositório no GitHub
2. Faça upload de **todo o conteúdo** desta pasta
3. Vá em **Actions** → **Build Jakesnap APK** → **Run workflow**
4. Ao terminar, baixe o artefato `Jakesnap-v1.0`

### Opção 2 — Local (Android Studio)

1. Abra a pasta `jakesnap` no Android Studio
2. Deixe o Gradle sincronizar
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. O APK estará em `app/build/outputs/apk/debug/app-debug.apk`
5. Renomeie para `Jakesnap-v1.0.apk`

### Opção 3 — Linha de comando

```bash
# Precisa de Android SDK + JDK 17
export ANDROID_HOME=/caminho/para/sdk
gradle assembleDebug
```

## Instalação no celular

1. Ative **Fontes desconhecidas** / **Instalar apps desconhecidos** nas configurações
2. Instale o APK `Jakesnap-v1.0.apk`
3. Abra o YouTube → compartilhe qualquer vídeo → escolha **Jakesnap**
4. Aguarde o download e toque em **Abrir no Lark Player**

## Ícone

O ícone atual é um **placeholder amarelo**. Substitua os arquivos em:

- `app/src/main/res/mipmap-*/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png`
- `app/src/main/res/drawable/ic_launcher_foreground.png`

por a foto do Jake Pudding (512x512 PNG quadrado).

## Estrutura

```
jakesnap/
├── app/
│   ├── src/main/
│   │   ├── java/org/jakesnap/downloader/
│   │   │   ├── MainActivity.kt
│   │   │   ├── DownloadViewModel.kt
│   │   │   ├── Downloader.kt
│   │   │   └── ui/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .github/workflows/build.yml
```

## Observações técnicas

- Min SDK 24 / Target SDK 34
- ABIs: arm64-v8a + armeabi-v7a
- `extractNativeLibs=true` e `useLegacyPackaging=true` (necessário para yt-dlp)
- R8 desabilitado
- Permissões de storage tratadas via MediaStore (Android 10+)
