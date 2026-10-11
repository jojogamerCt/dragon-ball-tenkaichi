# Funzionalità — Dragon Ball: Tenkaichi 0.1.0 (vertical slice)

Legenda: **Implementata** = codice + risorse + registrazioni + build verde su entrambi i loader;
la colonna "Verifica" indica come è stata provata (vedi `TESTING.md`).

| Area | Funzionalità | Stato | Verifica |
|---|---|---|---|
| Base | Build multi-loader, due JAR separati | Implementata | build |
| Base | Config comune/client versionate con migrazione e backup | Implementata | server dedicato |
| Base | Comandi `/tenkaichi stats|setstat|givetp|reset|unlockform|learn|restore|reload` | Implementata | client scriptato |
| Personaggio | Editor con 3 schede, anteprima 3D, rotazione/zoom, casuale | Implementata | client scriptato (screenshot) |
| Personaggio | Razze Saiyan e Umano (affinità, passive, start bonus) | Implementata | JUnit + client |
| Personaggio | 8 razze aggiuntive definite ma disabilitate (`implemented=false`) | Parziale | JUnit |
| Personaggio | Skin procedurale (pelle, occhi, sopracciglia, espressione, 3 abiti) o skin propria | Implementata | client scriptato |
| Personaggio | 7 acconciature selezionabili + 3 di trasformazione, coda Saiyan animata | Implementata | client scriptato |
| Personaggio | Altezza (attributo `scale`) e corporatura (larghezza modello) | Implementata | client scriptato |
| Statistiche | 12 statistiche, TP, costo superlineare, cap da Potenziale, pratica | Implementata | JUnit |
| Statistiche | Battle Power con forma/Ki/salute/fatica/soppressione | Implementata | JUnit |
| Progressione | Rendimenti decrescenti giornalieri, fatica, ricompense combattimento anti-farm | Implementata | JUnit |
| Progressione | Zenkai per Saiyan dopo quasi-morte contro nemici più forti | Implementata | non verificata in gioco |
| Combattimento | Combo, pesanti contestuali, counter, rush, guard break, clash corpo a corpo | Implementata | smoke server (IA) + client |
| Combattimento | Parata direzionale, perfect block, stamina di guardia | Implementata | codice, non verificata in gioco |
| Combattimento | Schivata i-frame, vanish, teleport alle spalle, dash, recupero aereo | Implementata | codice, non verificata in gioco |
| Combattimento | Hitstop, scossa camera, particelle, suoni d'impatto | Implementata | client scriptato |
| Combattimento | Lock-on con cambio bersaglio, reticolo, rilascio automatico | Implementata | client scriptato |
| Volo | Hover/normale/supersonico, inerzia, bang sonico, atterraggio con onda d'urto | Implementata | client scriptato |
| Ki | Carica, rigenerazione, esaurimento, Ki/stamina/BP in HUD | Implementata | client scriptato |
| Ki | Aura 3D procedurale (swirl + fiamme additive), 4 livelli di qualità | Implementata | client scriptato |
| Ki | Ki Blast con homing, Kamehameha con carica/sterzata/trincea | Implementata | smoke server + client |
| Ki | Beam Clash autoritativo con battiti, stagger, vittoria/pareggio | Implementata | non verificata in gioco |
| Mondo | Distruzione off/visual/real/regenerating, budget, protezioni | Implementata | smoke server (aria aperta) |
| Trasformazioni | Sistema data-driven + Super Saiyan (rabbia, cinematica, padronanza, drenaggio) | Implementata | client scriptato |
| NPC | Maestro: meditazione, controllo Ki, sparring, schivate, insegna il Kamehameha | Implementata | codice, verifica parziale |
| NPC | Predone Saiyan: FSM 10 stati, raffiche, cannone, schivate, seconda fase | Implementata | smoke server su entrambi i loader |
| UI | HUD, menu personaggio (statistiche/tecniche/forme/impostazioni), allenamento | Implementata | client scriptato |
| Oggetti | Fagiolo Senzu, uova di NPC, tab creativa | Implementata | registrazione verificata |
| Lingue | Inglese e italiano (256 chiavi) | Implementata | script di controllo chiavi |
| Audio | 34 eventi, 50 suoni sintetizzati originali | Implementata | generazione verificata |

## Non ancora implementato (dal documento di progetto)
Altre razze giocabili, trasformazioni oltre il Super Saiyan, tecniche aggiuntive, fusioni, Sfere del
Drago e Shenron, dimensioni (Namek, Stanza dello Spirito e del Tempo, Aldilà...), strutture, viaggio
spaziale, story mode, boss canonici, tornei, gilde/party, musica dinamica, mesh/shader personalizzati
oltre alle pipeline vanilla, LOD dedicato, benchmark.
