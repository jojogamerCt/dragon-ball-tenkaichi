import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi", "lang")

CHARACTERS = {
    "gohan": ("Gohan", "Gohan"),
    "krillin": ("Krillin", "Crilin"),
    "master_roshi": ("Master Roshi", "Maestro Muten"),
    "bulma": ("Bulma", "Bulma"),
    "raditz": ("Raditz", "Radish"),
    "piccolo": ("Piccolo", "Piccolo"),
    "nappa": ("Nappa", "Nappa"),
    "vegeta": ("Vegeta", "Vegeta"),
    "yamcha": ("Yamcha", "Yamcha"),
    "tien": ("Tien", "Tensing"),
    "chiaotzu": ("Chiaotzu", "Jiaozi"),
    "yajirobe": ("Yajirobe", "Yajirobei"),
    "king_kai": ("King Kai", "Re Kaio"),
    "bubbles": ("Bubbles", "Bubbles"),
    "gregory": ("Gregory", "Gregory"),
    "king_yemma": ("King Yemma", "Re Enma"),
    "saibaman": ("Saibaman", "Saibaman"),
    "kami": ("Kami", "Dio"),
    "cell_jr": ("Cell Junior", "Cell Junior"),
}

SCENES = {
    "reunion": [
        ("Hey! It's been ages! Come on in, everyone's here!", "Ehi! Quanto tempo! Entra, ci sono tutti!"),
        ("Everyone, this is Gohan, my student. Say hello, Gohan.", "Ragazzi, lui è Gohan, il mio allievo. Saluta, Gohan."),
        ("He's adorable! So you're a teacher now?", "Che carino! Quindi adesso fai il maestro?"),
        ("Heh heh, a fine young pupil. He has strong eyes.", "Eh eh, un bravo allievo. Ha uno sguardo deciso."),
        ("So this is where you've been hiding. The strongest fighter on this planet.", "Ecco dove ti nascondevi. Il guerriero più forte di questo pianeta."),
        ("Who are you? What do you want?", "Chi sei? Che cosa vuoi?"),
        ("I am Raditz, a Saiyan warrior. My race conquers planets... and I need strong fighters.", "Sono Radish, un guerriero Saiyan. La mia razza conquista pianeti... e mi servono combattenti forti."),
        ("Join me. Or refuse... and see what happens to this boy.", "Unisciti a me. Oppure rifiuta... e guarda cosa succede a questo bambino."),
        ("I'll never join you! Leave Gohan out of this!", "Non mi unirò mai a te! Lascia stare Gohan!"),
        ("Then the boy comes with me. Bring me a hundred dead Earthlings by tomorrow if you want him back!", "Allora il bambino viene con me. Portami cento terrestri morti entro domani, se lo rivuoi!"),
        ("He took Gohan... That guy was way too strong!", "Si è preso Gohan... Quel tipo era fortissimo!"),
    ],
    "alliance": [
        ("So you came. Have you brought me my hundred corpses?", "Allora sei venuto. Mi hai portato i miei cento cadaveri?"),
        ("Don't get the wrong idea. I'm not here to help you. I just want him gone.", "Non farti strane idee. Non sono qui per aiutarti. Voglio solo toglierlo di mezzo."),
        ("Piccolo... for once we fight on the same side.", "Piccolo... per una volta combattiamo dalla stessa parte."),
        ("Two against one won't change a thing! My power level is fifteen hundred!", "Due contro uno non cambierà nulla! La mia forza combattiva è millecinquecento!"),
        ("We'll see about that. Don't slow me down.", "Questo lo vedremo. Non rallentarmi."),
        ("Let's go! We're getting Gohan back!", "Andiamo! Ci riprendiamo Gohan!"),
        ("Come then! I'll crush you both!", "Fatevi sotto! Vi schiaccerò entrambi!"),
    ],
    "gohan_rage": [
        ("Pathetic. Your little alliance ends here.", "Patetici. La vostra piccola alleanza finisce qui."),
        ("Leave my master alone!!", "Lascia stare il mio maestro!!"),
        ("What?! The brat's power is over thirteen hundred!", "Cosa?! La forza del moccioso supera milletrecento!"),
        ("Piccolo! I've got him! Do it now!", "Piccolo! L'ho preso! Adesso!"),
        ("Let go of me! You'll die too, you fool!", "Lasciami! Morirai anche tu, idiota!"),
        ("Hold him still... just a little longer!", "Tienilo fermo... ancora un attimo!"),
        ("Don't worry about me! Fire!", "Non preoccuparti per me! Spara!"),
        ("Fools... two more Saiyans are coming... far stronger than me...", "Stolti... stanno arrivando altri due Saiyan... molto più forti di me..."),
        ("They'll arrive within a year... and this planet will be theirs...", "Arriveranno entro un anno... e questo pianeta sarà loro..."),
        ("A year. I'll train the boy myself. As for you... you're finished.", "Un anno. Allenerò io il ragazzo. Quanto a te... sei finito."),
        ("Do not despair. I will ask King Yemma to let you train in the Other World.", "Non disperare. Chiederò a Re Enma di farti allenare nell'Aldilà."),
        ("Then... see you in a year. Take care of Gohan...", "Allora... ci vediamo tra un anno. Abbi cura di Gohan..."),
    ],
    "king_yemma": [
        ("So you're the one Kami spoke of. A warrior who fell protecting the Earth.", "Quindi sei tu quello di cui parlava Dio. Un guerriero caduto per proteggere la Terra."),
        ("Please, King Yemma. Let this warrior train with King Kai.", "Ti prego, Re Enma. Lascia che questo guerriero si alleni con Re Kaio."),
        ("Snake Way is a million kilometres long. Nobody has ever made it to the end!", "La Via del Serpente è lunga un milione di chilometri. Nessuno è mai arrivato in fondo!"),
        ("Then I'll be the first.", "Allora sarò il primo."),
        ("Ha! Follow the road to its tail and you'll find King Kai's planet. Don't fall off!", "Ah! Segui la strada fino alla coda e troverai il pianeta di Re Kaio. Non cadere!"),
        ("Hurry. The Saiyans will reach the Earth in less than a year.", "Sbrigati. I Saiyan raggiungeranno la Terra in meno di un anno."),
    ],
    "king_kai_meeting": [
        ("Whoa... my body feels so heavy here!", "Wow... il mio corpo è pesantissimo qui!"),
        ("The gravity here is ten times the Earth's. Welcome to my planet!", "Qui la gravità è dieci volte quella terrestre. Benvenuto sul mio pianeta!"),
        ("So you want me to train you, eh? First... you have to make me laugh!", "Quindi vuoi che ti alleni, eh? Prima... devi farmi ridere!"),
        ("Uh... why did the Saiyan cross the road? To get to the other side!", "Ehm... perché il Saiyan ha attraversato la strada? Per arrivare dall'altra parte!"),
        ("Hahaha! That's terrible! I love it!", "Ahahah! Pessima! La adoro!"),
        ("Alright. Your first test: catch my pet monkey, Bubbles.", "D'accordo. Prima prova: acchiappa la mia scimmietta, Bubbles."),
        ("Once you can keep up with him in this gravity, we'll talk.", "Quando riuscirai a stargli dietro con questa gravità, ne riparleremo."),
        ("A monkey? Easy!", "Una scimmia? Facile!"),
    ],
    "bubbles_caught": [
        ("Not bad at all! Your body is adapting to the gravity.", "Niente male! Il tuo corpo si sta abituando alla gravità."),
        ("Next test: hit Gregory with this hammer.", "Prossima prova: colpisci Gregory con questo martello."),
        ("Bzzz! You'll never catch me!", "Bzzz! Non mi prenderai mai!"),
        ("We'll see about that!", "Questo lo vedremo!"),
    ],
    "gregory_hit": [
        ("Ouch! Okay, okay, you got me!", "Ahia! Va bene, va bene, mi hai preso!"),
        ("Excellent! You're faster than you think. Now the real training begins.", "Eccellente! Sei più veloce di quanto pensi. Ora inizia il vero allenamento."),
        ("I'm ready, King Kai!", "Sono pronto, Re Kaio!"),
    ],
    "king_kai_training": [
        ("First technique: the Kaioken. It multiplies your power by drawing out all your ki.", "Prima tecnica: il Kaioken. Moltiplica la tua forza facendo esplodere tutto il tuo ki."),
        ("Careful! Push it too far and it will tear your body apart.", "Attento! Se esageri ti farà a pezzi il corpo."),
        ("Now, my ultimate technique: the Spirit Bomb.", "E ora la mia tecnica suprema: la Genkidama."),
        ("Raise your hands. Gather energy from every living thing around you.", "Alza le mani. Raccogli l'energia di ogni essere vivente intorno a te."),
        ("Trees, animals, the air itself... borrow a little from each of them.", "Alberi, animali, l'aria stessa... prendi in prestito un po' da ognuno."),
        ("Only a pure heart can wield it. Use it only as a last resort.", "Solo un cuore puro può usarla. Usala solo come ultima risorsa."),
        ("Kaioken and Spirit Bomb... I won't let you down.", "Kaioken e Genkidama... non ti deluderò."),
        ("Practice both. The Saiyans won't wait for you!", "Allenati con entrambe. I Saiyan non ti aspetteranno!"),
    ],
    "wish": [
        ("The Dragon Balls have been gathered! They're wishing you back to life!", "Hanno radunato le Sfere del Drago! Stanno chiedendo di riportarti in vita!"),
        ("Your halo is gone. Go! Snake Way leads back to the check-in station.", "L'aureola è sparita. Vai! La Via del Serpente riporta all'ufficio di Re Enma."),
        ("Thank you for everything, King Kai!", "Grazie di tutto, Re Kaio!"),
        ("The Saiyans have already landed. Your friends are fighting right now!", "I Saiyan sono già atterrati. I tuoi amici stanno combattendo proprio ora!"),
        ("Hang on, everyone... I'm coming!", "Resistete, ragazzi... sto arrivando!"),
    ],
    "saiyans_land": [
        ("So this is the planet. Nappa, plant the Saibamen. Let's have some fun.", "Ecco il pianeta. Nappa, pianta i Saibaman. Divertiamoci un po'."),
        ("Six Saibamen, ready to play. Come on, Earthlings!", "Sei Saibaman pronti a giocare. Fatevi sotto, terrestri!"),
        ("Yamcha!! Those monsters... You'll pay for that!", "Yamcha!! Quei mostri... La pagherete!"),
        ("Goodbye, Tien...", "Addio, Tensing..."),
        ("Chiaotzu!! Tri-Beam... take everything I have left!!", "Jiaozi!! Kikoho... prenditi tutto quello che mi resta!!"),
        ("Heh. That tickled.", "Eh. Mi ha fatto il solletico."),
        ("Gohan... these years with you... were the only good ones I had... Run...", "Gohan... questi anni con te... sono stati gli unici belli della mia vita... Scappa..."),
        ("Mister Piccolo!!", "Signor Piccolo!!"),
        ("Hold on, everyone. Help is almost there!", "Resistete, ragazzi. I rinforzi stanno arrivando!"),
        ("Three hours have passed. Your hero isn't coming. Nappa, finish them.", "Sono passate tre ore. Il vostro eroe non verrà. Nappa, finiscili."),
    ],
    "goku_arrives": [
        ("This ki... it's enormous! It's coming this way!", "Questo ki... è enorme! Sta venendo qui!"),
        ("Master! You came back!", "Maestro! Sei tornato!"),
        ("Sorry I'm late. Leave the rest to me.", "Scusate il ritardo. Lasciate fare a me."),
        ("What?! His power level... it's over eight thousand?!", "Cosa?! La sua forza combattiva... supera ottomila?!"),
        ("Over eight thousand?! The scouter must be broken!", "Più di ottomila?! Lo scouter sarà rotto!"),
        ("Nappa, you idiot. He can hide his ki. Be careful.", "Nappa, idiota. Sa nascondere il suo ki. Stai attento."),
        ("You hurt my friends. Now you deal with me.", "Hai fatto del male ai miei amici. Adesso te la vedi con me."),
        ("Big talk! I'll crush you like a bug!", "Parli tanto! Ti schiaccerò come un insetto!"),
    ],
    "nappa_end": [
        ("It's over. Take your friend and leave this planet.", "È finita. Prendi il tuo amico e lascia questo pianeta."),
        ("Nappa. Get up.", "Nappa. Alzati."),
        ("Vegeta... help me... I can't move...", "Vegeta... aiutami... non riesco a muovermi..."),
        ("A Saiyan who cannot move is useless to me.", "Un Saiyan che non può muoversi non mi serve a niente."),
        ("You killed your own partner... You're a monster!", "Hai ucciso il tuo compagno... Sei un mostro!"),
    ],
    "beam_clash": [
        ("You'll regret this! Galick Gun!", "Te ne pentirai! Galick Gun!"),
        ("Kame... hame... HA!!", "Kame... hame... HA!!"),
        ("Kaioken... times three!!", "Kaioken... triplo!!"),
        ("Impossible! A low-class warrior overpowering me?!", "Impossibile! Un guerriero di basso livello che mi sovrasta?!"),
        ("Enough games... I'll show you the true power of a Saiyan!", "Basta giocare... ti mostrerò il vero potere di un Saiyan!"),
    ],
    "power_ball": [
        ("Do you know what a Saiyan becomes under a full moon? Of course you don't.", "Sai cosa diventa un Saiyan con la luna piena? Certo che no."),
        ("What is that light?", "Cos'è quella luce?"),
        ("Burst open and mix! A Power Ball, the artificial moon!", "Esplodi e mescolati! Una Power Ball, la luna artificiale!"),
        ("Now you will be crushed like an insect!", "Adesso verrai schiacciato come un insetto!"),
        ("He's... gigantic!", "È... gigantesco!"),
    ],
    "tail_cut": [
        ("Okay... nobody's looking... Tail, here I come!", "Bene... nessuno guarda... Coda, arrivo!"),
        ("My tail!! You worthless maggot!", "La mia coda!! Lurido verme!"),
        ("Everyone... lend me your energy. Just a little more...", "Tutti quanti... prestatemi la vostra energia. Ancora un po'..."),
        ("Trees, oceans, everyone on Earth... please...", "Alberi, oceani, abitanti della Terra... vi prego..."),
        ("Krillin! I can't move! Throw it for me!", "Crilin! Non riesco a muovermi! Lanciala tu!"),
        ("Got it! Here goes... Spirit Bomb!!", "Ricevuto! Ecco... Genkidama!!"),
        ("What is that?! I can't dodge it!", "Cos'è quella?! Non riesco a schivarla!"),
        ("We... we did it!", "Ce... ce l'abbiamo fatta!"),
        ("Impossible... me... the Prince of all Saiyans...", "Impossibile... io... il Principe dei Saiyan..."),
        ("I will not lose to scum like you!!", "Non perderò contro feccia come voi!!"),
    ],
    "vegeta_escapes": [
        ("No way... he's still standing!", "Non ci credo... è ancora in piedi!"),
        ("The brat still has his tail?! Curses!", "Il moccioso ha ancora la coda?! Maledizione!"),
        ("My body... I can't... I need my pod...", "Il mio corpo... non posso... mi serve la navicella..."),
        ("Now's my chance! I'll finish him with Yajirobe's sword!", "È la mia occasione! Lo finisco con la spada di Yajirobei!"),
        ("Krillin, wait. Let him go.", "Crilin, aspetta. Lascialo andare."),
        ("You'll regret sparing me... Next time, this planet will be mine!", "Ti pentirai di avermi risparmiato... La prossima volta questo pianeta sarà mio!"),
    ],
    "epilogue": [
        ("You're all back! ...Well, most of you.", "Siete tornati! ...Beh, quasi tutti."),
        ("Piccolo, Yamcha, Tien, Chiaotzu... without Kami, there are no Dragon Balls to bring them back.", "Piccolo, Yamcha, Tensing, Jiaozi... senza Dio non ci sono Sfere del Drago per riportarli indietro."),
        ("Then we'll find another way. There's another set of Dragon Balls... on planet Namek!", "Allora troveremo un altro modo. Esistono altre Sfere del Drago... sul pianeta Namecc!"),
        ("A journey into space, eh? You'd better rest up first.", "Un viaggio nello spazio, eh? Prima fareste meglio a riposarvi."),
        ("Namek... Wait for us. We'll bring everyone back.", "Namecc... Aspettateci. Riporteremo indietro tutti."),
    ],
}

TITLES = {
    "other_world": ("The Other World", "L'Aldilà"),
    "techniques": ("New techniques learned: Kaioken, Spirit Bomb", "Nuove tecniche apprese: Kaioken, Genkidama"),
    "meanwhile": ("Meanwhile, on Earth...", "Nel frattempo, sulla Terra..."),
    "vegeta_gone": ("Vegeta has left the Earth.", "Vegeta ha lasciato la Terra."),
    "saga_complete": ("Saiyan Saga - Complete", "Saga dei Saiyan - Completata"),
}

MISSIONS = {
    "saiyan_saga_01_reunion": ("A Peaceful Reunion", "Una rimpatriata tranquilla",
                               "Visit your old friends at Kame House with your young student Gohan.",
                               "Fai visita ai tuoi vecchi amici alla Kame House con il tuo giovane allievo Gohan.",
                               [("Go to Kame House", "Vai alla Kame House"), ("Reunion at Kame House", "Rimpatriata alla Kame House")]),
    "saiyan_saga_02_alliance": ("An Unlikely Alliance", "Un'alleanza inaspettata",
                                "Raditz has taken Gohan. Team up with Piccolo and get him back.",
                                "Radish ha rapito Gohan. Allea con Piccolo e riprenditelo.",
                                [("Reach Raditz's landing site", "Raggiungi il luogo di atterraggio di Radish"), ("Meet Piccolo", "Incontra Piccolo"),
                                 ("Defeat Raditz", "Sconfiggi Radish"), ("A desperate gamble", "Una mossa disperata")]),
    "saiyan_saga_03_snake_way": ("Snake Way", "La Via del Serpente",
                                 "In the Other World, run the whole of Snake Way to reach King Kai.",
                                 "Nell'Aldilà, percorri tutta la Via del Serpente per raggiungere Re Kaio.",
                                 [("Meet King Yemma", "Incontra Re Enma"), ("Run Snake Way to King Kai's planet", "Percorri la Via del Serpente fino al pianeta di Re Kaio"),
                                  ("Meet King Kai", "Incontra Re Kaio")]),
    "saiyan_saga_04_bubbles": ("Catch Bubbles", "Acchiappa Bubbles",
                               "Adapt to ten times the Earth's gravity by catching King Kai's monkey.",
                               "Abituati a una gravità dieci volte quella terrestre acchiappando la scimmia di Re Kaio.",
                               [("Catch Bubbles", "Acchiappa Bubbles"), ("King Kai's verdict", "Il verdetto di Re Kaio")]),
    "saiyan_saga_05_gregory": ("Hit Gregory", "Colpisci Gregory",
                               "Land a hit on the fast-flying Gregory.",
                               "Riesci a colpire il velocissimo Gregory.",
                               [("Hit Gregory", "Colpisci Gregory"), ("Gregory gives up", "Gregory si arrende")]),
    "saiyan_saga_06_techniques": ("King Kai's Techniques", "Le tecniche di Re Kaio",
                                  "Learn the Kaioken and the Spirit Bomb from King Kai.",
                                  "Impara il Kaioken e la Genkidama da Re Kaio.",
                                  [("Learn from King Kai", "Impara da Re Kaio"), ("Use the Kaioken", "Usa il Kaioken"), ("Use the Spirit Bomb", "Usa la Genkidama")]),
    "saiyan_saga_07_return": ("Back to Life", "Ritorno alla vita",
                              "The Dragon Balls brought you back. Return to Earth before it's too late.",
                              "Le Sfere del Drago ti hanno riportato in vita. Torna sulla Terra prima che sia troppo tardi.",
                              [("The wish", "Il desiderio"), ("Run back to the check-in station", "Torna di corsa all'ufficio di Re Enma"),
                               ("The Saiyans arrive", "Arrivano i Saiyan"), ("Reach the battlefield", "Raggiungi il campo di battaglia")]),
    "saiyan_saga_08_nappa": ("Nappa", "Nappa",
                             "Stop Nappa before he wipes out your friends.",
                             "Ferma Nappa prima che stermini i tuoi amici.",
                             [("Arrive on the battlefield", "Arriva sul campo di battaglia"), ("Defeat Nappa", "Sconfiggi Nappa"), ("Vegeta's cruelty", "La crudeltà di Vegeta")]),
    "saiyan_saga_09_vegeta": ("The Prince of Saiyans", "Il Principe dei Saiyan",
                              "Face Vegeta, the Saiyan prince, in the decisive battle.",
                              "Affronta Vegeta, il principe dei Saiyan, nella battaglia decisiva.",
                              [("Fight Vegeta", "Combatti contro Vegeta"), ("Beam clash", "Scontro di onde"), ("Push Vegeta to his limit", "Porta Vegeta al limite"),
                               ("The artificial moon", "La luna artificiale"), ("Defeat Great Ape Vegeta", "Sconfiggi Vegeta Oozaru"),
                               ("The tail and the Spirit Bomb", "La coda e la Genkidama"), ("Vegeta's retreat", "La ritirata di Vegeta")]),
    "saiyan_saga_10_epilogue": ("After the Storm", "Dopo la tempesta",
                                "Return to Kame House with the survivors.",
                                "Torna alla Kame House con i sopravvissuti.",
                                [("Go to Kame House", "Vai alla Kame House"), ("A new journey", "Un nuovo viaggio")]),
    "side_raider_hunt": ("Raider Hunt", "Caccia ai predoni",
                         "Saiyan raiders are terrorising the countryside. Defeat five of them.",
                         "I predoni Saiyan terrorizzano le campagne. Sconfiggine cinque.",
                         [("Defeat Saiyan raiders", "Sconfiggi i predoni Saiyan")]),
    "side_korin_water": ("The Sacred Water", "L'Acqua Sacra",
                         "Climb Korin Tower and earn the Sacred Water.",
                         "Scala la Torre di Karin e conquista l'Acqua Sacra.",
                         [("Reach Korin Tower", "Raggiungi la Torre di Karin"), ("Obtain the Sacred Water", "Ottieni l'Acqua Sacra")]),
    "side_roshi_lunch": ("Lunch for the Master", "Il pranzo del Maestro",
                         "Master Roshi is hungry. Bring six cooked cod to Kame House.",
                         "Il Maestro Muten ha fame. Porta sei merluzzi cotti alla Kame House.",
                         [("Deliver cooked cod to Kame House", "Consegna merluzzi cotti alla Kame House")]),
    "side_power_level": ("It's Over 9000!", "È più di 9000!",
                         "Read a power level above 9,000 with a scouter.",
                         "Leggi con uno scouter una forza combattiva superiore a 9.000.",
                         [("Scan a power level over 9,000", "Rileva una forza combattiva oltre 9.000")]),
    "side_time_chamber": ("The Room of Spirit and Time", "La Stanza dello Spirito e del Tempo",
                          "Reach Kami's Lookout and train in the Room of Spirit and Time.",
                          "Raggiungi il Palazzo di Dio e allenati nella Stanza dello Spirito e del Tempo.",
                          [("Reach Kami's Lookout", "Raggiungi il Palazzo di Dio"), ("Train in the Room of Spirit and Time", "Allenati nella Stanza dello Spirito e del Tempo")]),
    "side_turtle_training": ("Turtle School Training", "L'allenamento della Scuola della Tartaruga",
                             "Train hard until your Battle Power reaches 10,000.",
                             "Allenati duramente finché il tuo Battle Power non raggiunge 10.000.",
                             [("Reach 10,000 Battle Power", "Raggiungi 10.000 di Battle Power")]),
}

TECHNIQUES = {
    "double_sunday": ("Double Sunday", "Double Sunday"),
    "tri_beam": ("Tri-Beam", "Kikoho"),
    "hells_flash": ("Hell's Flash", "Hell's Flash"),
    "innocence_cannon": ("Innocence Cannon", "Cannone dell'Innocenza"),
    "perfect_kamehameha": ("Perfect Kamehameha", "Kamehameha Perfetta"),
    "spirit_bomb": ("Spirit Bomb", "Genkidama"),
    "death_ball": ("Death Ball", "Death Ball"),
    "planet_burst": ("Planet Burst", "Planet Burst"),
    "light_grenade": ("Light Grenade", "Light Grenade"),
    "spirit_ball": ("Spirit Ball", "Sfera dello Spirito"),
    "dodon_ray": ("Dodon Ray", "Raggio Dodonpa"),
    "eye_laser": ("Eye Laser", "Raggio dagli occhi"),
    "power_blitz": ("Power Blitz", "Power Blitz"),
    "evil_impulse": ("Evil Impulse", "Impulso Malvagio"),
    "vanishing_ball": ("Vanishing Ball", "Vanishing Ball"),
    "destructo_disc": ("Destructo Disc", "Kienzan"),
    "death_saucer": ("Death Saucer", "Disco della Morte"),
    "hellzone_grenade": ("Hellzone Grenade", "Hellzone Grenade"),
    "scattering_bullet": ("Scattering Bullet", "Proiettili a Dispersione"),
    "human_extinction_attack": ("Human Extinction Attack", "Attacco di Sterminio Umano"),
    "lightning_of_absolution": ("Lightning of Absolution", "Fulmine dell'Assoluzione"),
    "solar_flare": ("Solar Flare", "Taiyoken"),
    "instant_transmission": ("Instant Transmission", "Teletrasporto"),
    "kai_kai": ("Kai Kai", "Kai Kai"),
    "power_ball": ("Power Ball", "Power Ball"),
    "bomber_dx": ("Bomber DX", "Bomber DX"),
    "absorption": ("Absorption", "Assorbimento"),
    "candy_beam": ("Candy Beam", "Raggio Caramella"),
    "energy_absorption": ("Energy Absorption", "Assorbimento di Energia"),
    "barrier": ("Barrier", "Barriera"),
    "restoration": ("Restoration", "Guarigione"),
    "telekinesis": ("Telekinesis", "Telecinesi"),
    "stone_spit": ("Stone Spit", "Sputo Pietrificante"),
    "cell_junior": ("Cell Juniors", "Cell Junior"),
    "wolf_fang_fist": ("Wolf Fang Fist", "Colpo del Lupo"),
    "hakai": ("Hakai", "Hakai"),
}

EXTRA = {
    "hud.dbtenkaichi.mission_side": ("SIDE MISSION", "MISSIONE SECONDARIA"),
    "hud.dbtenkaichi.mission_story": ("STORY", "STORIA"),
    "hud.dbtenkaichi.scene_skip": ("%s to skip", "%s per saltare"),
    "hud.dbtenkaichi.scouter": ("SCOUTER", "SCOUTER"),
    "hud.dbtenkaichi.sensing": ("Sensing %s ki signatures", "Percepisci %s aure di ki"),
    "message.dbtenkaichi.absorb_too_strong": ("Your target is too strong to absorb.", "Il bersaglio è troppo forte per essere assorbito."),
    "message.dbtenkaichi.absorbed": ("Absorbed! Its power is now yours.", "Assorbito! Il suo potere ora è tuo."),
    "message.dbtenkaichi.absorbed_power": ("Absorption complete. Total absorptions: %s", "Assorbimento completato. Assorbimenti totali: %s"),
    "message.dbtenkaichi.form_rampage": ("You can't control this form yet!", "Non riesci ancora a controllare questa forma!"),
    "message.dbtenkaichi.gravity_heavy": ("The gravity here is crushing: ten times the Earth's!", "La gravità qui è schiacciante: dieci volte quella terrestre!"),
    "message.dbtenkaichi.great_ape_moon": ("You stare at the full moon... your body begins to change!", "Fissi la luna piena... il tuo corpo inizia a cambiare!"),
    "message.dbtenkaichi.mission_complete": ("Mission complete!", "Missione completata!"),
    "message.dbtenkaichi.mission_reward": ("%s complete! +%s TP", "%s completata! +%s TP"),
    "message.dbtenkaichi.moon_gone": ("The moon is out of sight. You return to normal.", "La luna non è più visibile. Torni normale."),
    "message.dbtenkaichi.no_tail": ("You need a tail to do that.", "Ti serve una coda per farlo."),
    "message.dbtenkaichi.returned_other_world": ("Your story continues in the Other World.", "La tua storia continua nell'Aldilà."),
    "message.dbtenkaichi.scouter_9000": ("It's over 9000!!", "È più di 9000!!"),
    "message.dbtenkaichi.scouter_overload": ("Your scouter overloaded and exploded!", "Il tuo scouter è andato in sovraccarico ed è esploso!"),
    "message.dbtenkaichi.sense_off": ("Ki sense: off", "Percezione del ki: disattivata"),
    "message.dbtenkaichi.sense_on": ("Ki sense: on", "Percezione del ki: attiva"),
    "message.dbtenkaichi.side_mission": ("Side Mission", "Missione secondaria"),
    "message.dbtenkaichi.snake_way_fall": ("You fell off Snake Way! Back onto the road.", "Sei caduto dalla Via del Serpente! Torni sulla strada."),
    "message.dbtenkaichi.story_fight": ("Fight!", "Combatti!"),
    "message.dbtenkaichi.story_mission": ("Story Mission", "Missione della storia"),
    "message.dbtenkaichi.tail_cut": ("Your tail has been cut off!", "Ti hanno tagliato la coda!"),
    "message.dbtenkaichi.tail_free": ("Your tail is free.", "La tua coda è libera."),
    "message.dbtenkaichi.tail_grabbed": ("Your tail was grabbed! Your strength drains away!", "Ti hanno afferrato la coda! Le forze ti abbandonano!"),
    "message.dbtenkaichi.tail_lost_revert": ("Without a tail you return to normal.", "Senza coda torni normale."),
    "message.dbtenkaichi.tail_regrown": ("Your tail has grown back.", "La tua coda è ricresciuta."),
    "message.dbtenkaichi.tail_wrapped": ("You wrap your tail around your waist.", "Avvolgi la coda intorno alla vita."),
    "message.dbtenkaichi.technique_cooldown": ("%s is still recharging.", "%s si sta ancora ricaricando."),
    "message.dbtenkaichi.technique_no_target": ("No target in sight.", "Nessun bersaglio in vista."),
    "message.dbtenkaichi.technique_requirement": ("You don't meet this technique's requirements yet.", "Non soddisfi ancora i requisiti di questa tecnica."),
    "message.dbtenkaichi.form_hint.moon": ("%s awakens under a full moon or a Power Ball, if you have a tail.", "%s si risveglia sotto la luna piena o una Power Ball, se hai la coda."),
    "requirement.dbtenkaichi.required_form": ("the required previous form", "la forma precedente richiesta"),
    "requirement.dbtenkaichi.tail": ("a tail", "una coda"),
    "requirement.dbtenkaichi.absorptions": ("enough absorptions", "abbastanza assorbimenti"),
    "requirement.dbtenkaichi.mission": ("enough story progress", "abbastanza progressi nella storia"),
    "scene.dbtenkaichi.title.other_world": None,
    "screen.dbtenkaichi.character.forms_hint": ("Set a form as Target: the transform key will climb toward it.", "Imposta una forma come Obiettivo: il tasto trasformazione salirà verso di essa."),
    "screen.dbtenkaichi.character.req_absorb": ("Absorptions: %s/%s", "Assorbimenti: %s/%s"),
    "screen.dbtenkaichi.character.req_moon": ("Look at the full moon (or a Power Ball) with your tail out", "Guarda la luna piena (o una Power Ball) con la coda libera"),
    "screen.dbtenkaichi.character.req_tail": ("Requires a tail", "Richiede la coda"),
    "screen.dbtenkaichi.character.target": ("Target", "Obiettivo"),
    "screen.dbtenkaichi.character.target_set": ("Target ✔", "Obiettivo ✔"),
    "screen.dbtenkaichi.missions": ("Missions", "Missioni"),
    "screen.dbtenkaichi.missions.accept": ("Accept", "Accetta"),
    "screen.dbtenkaichi.missions.active": ("Active", "Attive"),
    "screen.dbtenkaichi.missions.available": ("Available", "Disponibili"),
    "screen.dbtenkaichi.missions.completed": ("Missions completed: %s", "Missioni completate: %s"),
    "screen.dbtenkaichi.missions.repeat": ("Repeatable - %s TP", "Ripetibile - %s TP"),
    "screen.dbtenkaichi.missions.requires_bp": ("Requires %s BP", "Richiede %s BP"),
    "screen.dbtenkaichi.missions.reward": ("Reward: %s TP", "Ricompensa: %s TP"),
    "screen.dbtenkaichi.missions.start_story": ("Start", "Inizia"),
    "screen.dbtenkaichi.missions.waypoint": ("World marker: %s", "Indicatore nel mondo: %s"),
    "screen.dbtenkaichi.settings.sense": ("Toggle ki sense", "Attiva/disattiva percezione del ki"),
    "screen.dbtenkaichi.settings.tail": ("Tail: %s", "Coda: %s"),
    "screen.dbtenkaichi.settings.tail_free": ("Free", "Libera"),
    "screen.dbtenkaichi.settings.tail_wrapped": ("Wrapped", "Avvolta"),
    "screen.dbtenkaichi.settings.tail_lost": ("Cut off", "Tagliata"),
    "key.dbtenkaichi.ki_sense": ("Ki Sense", "Percezione del Ki"),
    "key.dbtenkaichi.missions": ("Missions", "Missioni"),
    "key.dbtenkaichi.skip_scene": ("Skip Cutscene", "Salta filmato"),
    "item.dbtenkaichi.scouter": ("Scouter", "Scouter"),
    "item.dbtenkaichi.majin_candy": ("Majin Candy", "Caramella Majin"),
    "entity.dbtenkaichi.story_character": ("Story Character", "Personaggio della storia"),
    "entity.dbtenkaichi.power_ball": ("Power Ball", "Power Ball"),
    "biome.dbtenkaichi.wasteland": ("Rocky Wasteland", "Landa rocciosa"),
}


def build():
    en, it = {}, {}

    def add(key, e, i):
        en[key] = e
        it[key] = i

    for cid, (e, i) in CHARACTERS.items():
        add("character.dbtenkaichi." + cid, e, i)
    for sid, lines in SCENES.items():
        for n, (e, i) in enumerate(lines, 1):
            add("scene.dbtenkaichi.%s.%d" % (sid, n), e, i)
    for tid, (e, i) in TITLES.items():
        add("scene.dbtenkaichi.title." + tid, e, i)
    for mid, (te, ti, de, di, objectives) in MISSIONS.items():
        add("mission.dbtenkaichi." + mid, te, ti)
        add("mission.dbtenkaichi." + mid + ".desc", de, di)
        for n, (e, i) in enumerate(objectives):
            add("mission.dbtenkaichi.%s.objective.%d" % (mid, n), e, i)
    for tid, (e, i) in TECHNIQUES.items():
        add("technique.dbtenkaichi." + tid, e, i)
    for key, value in EXTRA.items():
        if value is not None:
            add(key, value[0], value[1])
    return en, it


def merge(name, entries):
    path = os.path.join(LANG, name + ".json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data.update(entries)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(dict(sorted(data.items())), f, ensure_ascii=False, indent=2)
        f.write("\n")


def main():
    en, it = build()
    merge("en_us", en)
    merge("it_it", it)
    print("merged %d story keys" % len(en))


if __name__ == "__main__":
    main()
