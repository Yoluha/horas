# Horas

App Android (Kotlin nativo) para picar ponto — entrada/saída, nota opcional,
histórico por dia, e um botão para enviar o registo ao patrão via WhatsApp.

Ver [briefing.md](briefing.md) para o contexto completo e decisões tomadas.

## Estado

v1 (MVP) está escrita e **já compila** — `gradlew assembleDebug` corrido com sucesso,
APK gerado em `app/build/outputs/apk/debug/app-debug.apk`.
A deteção automática por WiFi (v2) ainda não está implementada.

## Ambiente já preparado

Android Studio, o Android SDK (platform-tools, Android 14, build-tools) e o Gradle
já estão instalados e configurados neste PC. `local.properties` já aponta para o SDK.

Toolchain usada (versões recentes, ajustadas ao Android Studio instalado):
- AGP 9.3.0 com suporte Kotlin "built-in" (sem plugin `org.jetbrains.kotlin.android`)
- KSP 2.3.11 para o Room (em vez de kapt, que deixou de ser compatível com o AGP 9)
- Gradle 9.7.1, Room 2.8.4

## Como abrir e correr

1. Abre o Android Studio → **Open** → escolhe a pasta `G:\AppHorasTrabalhadas`.
2. O Gradle deve sincronizar sem pedir mais nada (SDK e wrapper já resolvidos).
3. Para testar:
   - **Telemóvel físico** (recomendado — o WhatsApp real só existe no telemóvel):
     ativa "Opções de programador" → "Depuração USB", liga por USB, escolhe o
     telemóvel na lista de dispositivos → Run.
   - **Emulador**: Android Studio → Device Manager → cria um dispositivo virtual → Run.

## Antes de usar a sério

Abre o app → ícone de definições (engrenagem) → escreve o número do patrão com
indicativo (ex: `+351912345678`) → Guardar. Sem isso, o botão de enviar mensagem
avisa que falta o número.

## Estrutura do código

- `data/` — Room (base de dados local): `PunchEntity`, `PunchDao`, `AppDatabase`,
  e `SettingsStore` (número do patrão, guardado em SharedPreferences).
- `domain/` — lógica pura: `HoursCalculator` (agrupa picos por dia e soma horas),
  `MessageBuilder` (texto da mensagem a enviar).
- `util/` — `TimeUtils` (formatação de datas/horas), `WhatsAppLauncher` (abre o
  WhatsApp via link `wa.me` com o texto pré-preenchido).
- `MainActivity` — ecrã principal (entrada/saída/nota/hoje).
- `SettingsActivity` — número do patrão.
- `history/` — `HistoryActivity` (lista de dias), `DayDetailActivity` (detalhe de
  um dia + enviar esse dia), `DayAdapter` (RecyclerView).
