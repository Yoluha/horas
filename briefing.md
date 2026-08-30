# App de Horas Trabalhadas — Briefing

Referência: parecido com "Ponto Certo" (Play Store), mas mais simples e focado.

## Problema a resolver
Esquece-se muitas vezes de picar o ponto no trabalho. O patrão pergunta a que horas
entrou/saiu e não há registo fácil para mostrar.

## Funcionalidades essenciais
1. **Entrada / Saída** — um botão grande para picar (entrada), outro para picar saída.
   Regista a hora automaticamente ao tocar. Sem fricção, sem formulários.
2. **Nota/observação** — campo de texto opcional em cada registo (ex: "saí mais cedo,
   dentista", "cheguei atrasado, trânsito").
3. **Enviar como mensagem** — botão para enviar o registo do dia (ou de um intervalo)
   como texto directo ao patrão via WhatsApp (`wa.me` com texto pré-preenchido, tal
   como decidido para o app de encomendas). Não é PDF aqui — é texto rápido tipo
   "Hoje: entrada 09:03, saída 18:12".
4. **Histórico** — ecrã para consultar dias anteriores e ver total de horas por dia/
   semana/mês.
5. **Deteção automática por WiFi (nice-to-have, fase 2)** — quando o telemóvel liga/
   desliga da rede WiFi do trabalho, regista essa hora como sugestão de presença
   (não como ponto oficial — é um registo paralelo "estava ligado à rede X das 09:05
   às 18:10" para cruzar com o ponto manual).

## Notas técnicas a decidir
- **Permissões WiFi**: no Android, ler o nome (SSID) da rede WiFi ligada exige
  permissão de localização (ACCESS_FINE_LOCATION) em runtime, mesmo não sendo GPS.
  Detetar em segundo plano de forma contínua tem restrições (Android 10+) e consome
  bateria — é preciso confirmar se compensa fazer isto na v1 ou deixar para depois
  do ponto manual estar sólido.
- **Base de dados**: local (Room/SQLite), sem servidor — como decidido no plano geral.
- **Stack**: Kotlin nativo.

## Decisões tomadas (2026-08-30)
- Nome do app: **Horas**. Package id: `com.lucas.horas`. SDK mínimo: Android 7.0 (API 24).
- Contacto do patrão: **número fixo**, guardado uma vez em Definições.
- Deteção por WiFi: **fica para v2** — v1 é só ponto manual + nota + envio + histórico.
- Horário normal (diário/semanal) — ainda não é necessário; só entra se/quando
  quiser cálculo de horas extra num v3.

## Estado
v1 está escrita (código Kotlin completo em `app/src/main/...`). Falta: instalar
Android Studio para compilar/testar, e depois testar no telemóvel real.
