# Problemi noti e limitazioni — 0.2.0

Classificazione: **Alta** (blocca funzioni importanti), **Media** (degrada l'esperienza),
**Bassa** (cosmetica o marginale).

| Gravità | Problema | Note |
|---|---|---|
| Media | Prestazioni non misurate | L'aura e i raggi generano geometria per frame (fino a ~20 fiamme × 6 segmenti × 4 strati). Livelli Low/Medium riducono il carico ma mancano benchmark. |
| Media | Multiplayer con più giocatori reali non provato | La logica è autoritativa e i pacchetti sono inviati ai tracker, ma il test reale è stato fatto con un solo client. |
| Media | Trasformazioni solo per Saiyan e Mezzo-Saiyan | Super Saiyan, SS2 e SS3 (più il Kaioken per tutti); le altre razze non hanno ancora forme proprie. |
| Bassa | Nuvole della cutscene SSJ3 | Le nuvole vanilla corrono avanti e indietro solo se sono attive nelle opzioni e se in quel punto del cielo ce ne sono; gli sbuffi di nuvole generati dalla cutscene sono sempre visibili. |
| Media | Prove dei Maestri non provate con input umano | Teletrasporto, conto alla rovescia, vittoria forzata e ritorno sono verificati dal test client su entrambi i loader; difficoltà e premi non sono stati giocati. |
| Bassa | La Kame House può essere lontana dallo spawn | Viene costruita nell’oceano più vicino (ricerca fino a ~1,6 km); `/tenkaichi landmarks` mostra le coordinate. |
| Bassa | Breve rallentamento alla prima generazione dei luoghi iconici | La costruzione carica i chunk necessari al primo avvio del mondo (circa 2-3 s). |
| Bassa | Karin senza bastone | Il bastone di Karin non è ancora modellato. |
| Bassa | Posa di carica del Galick Gun poco leggibile di fronte | Le braccia sono portate dietro il corpo ruotato. |
| Bassa | Tecniche a sfera con ricarica condivisa | Big Bang Attack e Raggio della Morte usano lo stesso tempo di ricarica dei colpi Ki. |
| Bassa | Pianeta di Re Kaioh rimosso di proposito | Tornerà con l’Aldilà; nel frattempo il Kaioken si compra dalla lista tecniche. |
| Media | Bilanciamento del Predone e combo breaker non provati con input umano | Danni ridotti (×0,6), cadenza più lenta e immunità allo stordimento dopo 3 colpi sono verificati solo nel codice e nello smoke test dell'IA. |
| Media | Durata delle sessioni di sparring/schivata non provata dal vivo | La causa dell'interruzione precoce (Maestro cercato entro 8 blocchi) è stata corretta (40 blocchi, sessioni da 60 s e 45 s), ma non è stata giocata una sessione reale. |
| Bassa | Le kick non hanno animazione in prima persona | In prima persona sono visibili solo pugni e parata; i calci danno solo un leggero scossone della camera. |
| Bassa | Particelle traslucide, non additive | Minecraft 26.3 non offre una pipeline additiva per le particelle; il bagliore usa texture morbide a piena luminosità. |
| Media | Qualità dei suoni non ascoltata | I suoni sono generati e verificati solo con misure (picchi, livelli, spettrogrammi): vanno ascoltati e, se serve, ritoccati in `tools/gen_sounds.py`. |
| Bassa | Braccia senza gomiti nella posa della Kamehameha | Il modello di Minecraft ha braccia rigide: la posa usa braccia dritte che convergono sul fianco destro. |
| Bassa | Camera della cutscene | La camera evita i blocchi accorciando la distanza, ma in spazi stretti alcune inquadrature risultano molto vicine. |
| Media | Bilanciamento delle passive razziali non provato in gioco | Gli effetti sono attivi lato server e le razze risultano distinte nei test JUnit, ma i valori non sono stati provati in partite reali. |
| Bassa | Modelli razziali a cubi | Antenne, corna, cresta e orecchie sono composte da cubi segmentati (stile Minecraft), non da mesh curve. |
| Media | Spawn naturale del Predone non implementato | Si ottiene con uovo o `/summon dbtenkaichi:saiyan_raider`. |
| Bassa | Capelli Super Saiyan ancora compatti | Le punte sono state allargate, ma da lontano formano ancora una sagoma alta e stretta. |
| Bassa | Corpo intero inclinato in volo anche per gli altri giocatori sulla base della velocità stimata | Su connessioni con forte jitter l'inclinazione può tremolare. |
| Bassa | La modalità di distruzione `regenerating` usa una coda in memoria | Allo spegnimento del server i blocchi vengono ripristinati; un crash del server li lascerebbe distrutti. |
| Bassa | Integrazione con mod di protezione | Passa per `PlayerBlockBreakEvents.BEFORE` (Fabric) e `BreakBlockEvent` (NeoForge); nessuna integrazione esplicita con mod di claim specifiche. |
| Bassa | NeoForge per 26.3 è solo in beta | Non esiste ancora una NeoForge stabile per 26.3. |
| Bassa | Il mixin anti-kick del volo usa `tick()` | Il primo tentativo con `@ModifyExpressionValue` dentro `handleMovePlayer` falliva solo su Fabric ("Scanned 0 target(s)"); sostituito con un'iniezione in `tick()` che azzera il contatore di galleggiamento, verificata su entrambi i loader. |
| Bassa | Gli smoke test sono inclusi nel JAR | Restano inattivi senza le proprietà di sistema `dbtenkaichi.smokeTest` / `dbtenkaichi.clientSmoke`. |

Ambiente di sviluppo (non della mod): un antivirus che intercetta HTTPS impedisce a Java di
scaricare dipendenze; `scripts/gw.sh`/`gw.ps1` usano il trust store di Windows. I messaggi
"Perflib"/"PerfCounter" nei log provengono da OSHI e sono innocui.
