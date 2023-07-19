# Stencil CMS Alpha Käyttöohje

## Sisältö

* [Esittely](#Introduction)
* [Ominaisuudet](#Features)
  * [Artikkelit](#Articles)
  * [Sivut](#Pages)
  * [Linkit](#Links)
  * [Lomakkeet ja työnkulut](#Forms-and-workflows)
  * [Paikannukset](#Localisations)
  * [Julkaisut](#Releases)
* [Stencil-käyttöliittymän esittely](#Intro-to-UI-Layout)
* [Ensimmäisten resurssien luominen](#Creating-your-first-resources)
* [Resurssien poistaminen ja irrottaminen](#Deleting-and-removing-resources)



### Esittely

Stencil CMS on sisällönhallintajärjestelmä, joka on kehitetty luomaan ja hallinnoimaan staattista sisältöä usealla kielellä käyttäen [Markdown]-järjestelmää (https://www.markdownguide.org/). 

Stencil koostuu kahdesta osasta: 

* **Stencil CMS**: Pääasiallinen sovellus staattisen sisällön hallintaan, suunniteltu toimimaan yhdessä organisaatiosi Stencil Portal -käyttöliittymän kanssa.
* **Stencil Portal Käyttöliittymä**: Staattisen sisällön sivusto, jossa käyttäjät pystyvät lukemaan sisältöäsi, täyttämään lomakkeita, vastaanottamaan viestejä organisaatioltasi ja muuta. 

Tämä alusta vakauttaa ja yhdistää sisältösi, datankeruun ja prosessien automatisoinnin yhteen tilaan. 

Keskeiset filosofiat:

**Monikielisen sisällön tuottamisen tulisi olla tehokasta ja yksinkertaista.**  

Nykypäivänä monikielisen sisällön tuottaminen ja hallinta on olennaista. Kaiken Stencilissä on tarkoitus virtaviivaistaa sisällön tuottamisen ja kääntämisen prosesseja. Esimerkiksi Stencilin avulla kääntäjät voivat nähdä alkuperäisekielisen tekstin kirjoittaessaan käännettyä teksiä halkaistun näytön avulla. Aikaa säästyy ja työ helpottuu, kun ei ole tarpeen vaihdella näkymää ikkunasta toiseen. 

**Sisällön tuottajien ei tulisi tarvita hallita sisältöään useissa eri paikoissa samaan aikaan.**  

Stencilin avulla voit asettaa käännökset, linkit, kielet ja työnkulut kerran ja käyttää näitä resursseja ja niiden käännöksiä laajasti tai yksittäisesti. Jos linkin tekstiä tarvitsee muuttaa, se tehdään **yhdessä** paikassa ja muutokset päivittyvät kaikkialle missä linkkiä käytetään. Tai esimerkiksi on lomake, joka on useassa paikassa sovelluksessasi. Riittää, että lomaketta muokataan kerran, jotta se päivittyy kaikkialle missä se on käytössä. Näin vapautuu enemmän aikaa uuden sisällön tuottamiseen, kun ei tarvitse huolehtia siitä, miten monessa paikassa sisältöä pitää muokata.

**Datankeruu ja prosessien automatisointi ovat ensisijaisia elementtejä CMS ratkaisun kokonaisuudessa.**  

Sisällön tarjoaminen käyttäjille on mahtavaa. Parempaa vielä on kuitenkin tarjota käyttäjille mahdollisuus tehdä toimintoja sisällön kanssa sekä suunnitella prosesseja toimimaan tämän datan perusteella. Stencil toimii kahden muun alustan kanssa tarjotakseen lomake- ja prosessipalveluita. Uskomme, että organisaatioiden tulisi pystyä hallitsemaan kaikkia näitä kolmea elementtiä luodakseen täydellisesti tarpeisiinsa sopivan ratkaisun. 

**Versioiden hallinta on ehdottoman tärkeää.**  

Aikaa ja vaivaa säästyy, kun pidetään sekä yhtä todenmukaista lähdettä, että tarkistettua muutoshistoriaa. Tästä syystä Stencilin Release-ominaisuus mahdollistaa yksinkertaisten merkintöjen ja lisähuomioiden tekemisen, jolloin on mahdollista palata aikaisempaan työhön koska tahansa, tehdä vikatarkastuksia tai palauttaa työ aikaisempaan vaiheeseen.  

---

### Ominaisuudet

* Yksinkertainen, monikielinen sisältörakenne Markdown syntaksilla
* Samanaikainen, halkaistun näytön sisällönmuokkaus kahdelle kielelle (Tuplanäkymä)
* Yhdistä lomakkeet ja niiden työnkulut staattiseksi sisällöksi
* Versionnin ja julkaisun hallintajärjestelmä 
* Paikannettavuus kaikelle sisällölle, linkeille, lomakkeille ja työnkuluille 

---

## Käyttäjän perusopas

Stencil koostuu kuudesta eri elementistä, joihin viitataan "resursseina". Resursseja ovat: 

* **Artikkelit**: Sisällyttävät ryhmittymiseen liittyvät resurssit 
* **Sivut**: Paikannettua Markdown-sisältöä, suorassa yhteydessä artikkeleihin   
* **Linkit**: Paikannettuja sisäisiä tai ulkoisia linkkejä 
* **Paikannukset**: Saatavilla olevat kielet sisällölle 
* **Julkaisut**: Versiot / kuvat sisällön kokonaisuudesta annettuna aikana
* **Työnkulut**: Paikannetut lomakkeet ja prosessit yhteydessä artikkeleihin / sivuihin 


### Artikkelit

Artikkelit voidaan käsittää eräänlaisina lokeroina ryhmittymiseen liittyville elementeille/resursseille. Artikkeli on keskus, joka yhdistää sivut (paikannettu sisältö), linkit ja työnkulut. 

![Articles](readme-images/stencil-article-association.png)



Artikkelilla **täytyy** olla nimi (nimi muokattavissa) ja ainakin yksi sivu. Nimellä on vain käytännöllinen tarkoitus, eikä se näy käyttöpuolella lainkaan. Artikkelit, joissa ei ole sivuja eivät kirjaudu käyttöliittymälle.  

Artikkelin edessä **täytyy** olla kolminumeroinen luku. Numero määrittää sen, missä järjestyksessä se näkyy valikossa.  

Esimerkki artikkelin nimestä ja järjestysnumerosta: `100_Housing`

Artikkeliin **voi** olla yhdistetty linkkejä ja työnkulkuja, mutta se ei ole pakollista. 

Artikkeleita voodaan kerrostaa korkeintaan **kahdelle** tasolle. On mahdollista olla yksi emoartikkeli ja yksi tytärartikkeli. Alla esimerkki kerrostetusta artikkelista: 

`150_Construction/100_Building_Permits`

Artikkelien käyttöpuolella näkyvät nimet tulevat ensimmäisestä Markdown Level 1 otsikosta kullakin sivulla. 


Esimerkkitilanne: Sinulla on artikkeli, jolla on kaksi sivua: `en.md` ja `et.md`. Haluat tämän artikkelin näkyvän käyttäjille englanniksi nimellä "Animals", mutta haluat varmistaa, että viron kielen valinneelle käyttäjälle nimi näkyy vironkielisenä: "Loomad". Tehdäksesi näin yksinkertaisesti annat jokaiselle sivulle Level 1 otsikon oikealla nimellä, jolloin järjestelmä ottaa nimet käyttöön. 

en.md

```markdown

# Animals

```

et.md

```markdown

# Loomad

```


---

### Sivut  

Sivut ovat Markdown-tiedostoja. Ne ovat nimettyjä sisältönsä kielen mukaan ja sisältävät vain määrätyn kielistä sisältöä. Esimerkiksi sivulla `en.md` on vain englanninkielistä sisältöä. 

Perussääntö on 1 sivu == 1 sijainti. Artikkelissa voi olla miten paljon sivuja tahansa, riippuen esitettävien kielten määrästä. Kuitenkin on mahdollista olla vain YKSI sivu artikkelia kohti samassa sijainnissa. 

Koska sivu on suorassa yhteydessä sijaintiin sen olemassaolo on riippuvainen halutun sijainnin olemassaolosta. Jos siis halutaan luoda sivuja kielellä, jota ei olla vielä lisätty on ensin luotava sijainti, jotta voidaan luoda sivuja käyttämään tätä sijaintia. 

Sivulla **täytyy** olla ainakin 1 Markdown Level 1 otsikko. Ensimmäinen tällainen otsikko sivulla toimii paikannettuna nimenä arikkelille, jonka käyttäjä näkee sisältövalikossa. 

Sivulla ei ole muokattavaa nimeä. Sivun nimi on aina [kaksikirjaiminen kielitunniste](https://en.wikipedia.org/wiki/ISO_639-1), joka vastaa sivun sisällön kieltä ja jota seuraa `.md` tiedostotunniste. Esimerkiksi `sv.md` on ruotsinkielinen Markdown-sivu.  
A Page is specific to an Article, and it cannot exist outside of an Article. 

Tiivistettynä, ennen sivun luomista on muistettava kolme asiaa: I

* Artikkelin, jossa sivun halutaan olevan tulee olla olemassa jo etukäteen. 
* Sivun sisällön sijainnin tulee olla etukäteen olemassa.
* Sivulla tulee olla Markdown Level 1 otsikko, joka näyttäytyy sivun aiheena käyttäjille sisältövalikossa. 

---

### Linkit

Linkit voivat olla **sisäisiä**, **ulkoisia** tai **puhelinnumeroita**. Sisäinen linkki yhdistyy domainin sisäisille sivuille, kun taas ulkoiset linkit yhdistyvät domainin ulkopuolisille sivuille. "Puhelin" -tyyppiset linkit näyttäytyvät puhelinnumeroina käyttöliittymässä. 

Linkit voivat liittyä mihin tahansa, kaikkiin tai ei mihinkään artikkeleihin / sivuihin.

Linkit ovat paikannettuja. Sijainti, joka linkille määritetään sitä luotaessa määrittää linkit näkymään vain sivuilla, jotka ovat tässä sijainnissa. Alla esimerkkitilanne: 

On artikkeli `100_Housing`. Artikkelilla on kaksi sivua, `en.md` ja `sv.md`. Luodaan linkki, jonka sijainti on `en`. Linkki yhdistetään artikkeliin `100_Housing`. Linkki tulee näkymään vain sivulla `en.md`, vaikka se yhdistettiin koko artikkeliin. Jos linkin halutaan näkyvän myös sivulla `sv.md` sille tulee antaa tämän mukainen paikannus.

Jos halutaan linkin näkyvän KAIKILLA artikkelin sivuilla, kielestä huolimatta, jätetään sijaintikenttä tyhjäksi linkkiä luodessa. 

Kun linkki on luotu se on saatavilla koko sovelluksessa.
 
Jos jonkin linkin ei haluta näkyvän tietyllä sivulla enää, linkin yhteys Artikkeliin voidaan erottaa poistamatta linkkiä sovelluksesta.  

Kun linkki poistetaan, se poistuu sovelluksesta kokonaan. 

---

### Lomakkeet ja työnkulut

Stencil on suunniteltu kokoamaan sisältösi, datankeruusi ja prosessiesi automatisoinni yhteen vakaaseen sovellukseen. Käytännössä voit siis asettaa staattisen sisältösi ohjaamaan käyttäjät lomakkeittesi pariin ja luoda työnkulkuja määrittämään mitä tapahtuu, kun näihin lomakkeisiin on vastattu.  

Pidetään esimerkkinä tilannetta, jossa organisaatiosi haluaa tarjota käyttäjille lomakkeen, jolla tiedustellaan/kysellään jostain tuotteesta. Kun käyttäjä palauttaa täytetyn lomakkeen, hallinnoija/administraattori saa tästä ilmoituksen ja toimii sen mukaisella tavalla. Tämä reaktio voi olla myös automatisoitu ja on täysin mallinnettavissa tarpeidesi mukaan.  

---

### Paikannukset

Paikannukset (sijainnit) mahdollistavat siältösi kielen määrittämisen. Paikannuksia voidaan määrittää niin paljon kuin on tarpeen.  
  
Sijainnit luodaan määrittämällä kieli [kaksikirjaimisen kielitunnisteen](https://en.wikipedia.org/wiki/ISO_639-1) perusteella. Järjestelmä käyttää näitä kielitunnisteita myös nimeämään sivuja. Kun luodaan sijainti englanninkielelle (`en`) ja tähän sijaintiin lisätään sivuja, sivujen nimeksi tulee automaattisesti `en.md`. 

Kun sijainti on luotu, se on saatavilla koko sovelluksessa. Tällöin voidaan alkaa luomaan sisältöjä kunten sivuja, linkkejä jne. paikantamalla niitä tähän sijaintiin. 

Stencil tarjoaa myös "Locale Usage Overview" (Sijainninkäytön yhteenveto) -ominaisuuden, jonka avulla voidaan seurata helposti, millä artikkeleilla on tai ei ole sivuja kullekkin aktivoidulle kielelle. Myös artikkeli - sijainti käyttötaulukko on saatavilla, mikä näyttää koko sivuston yhteenvedon seuraavista asioista: 

* Kaikki sivusi artikkelit ja sijainnit 
* Artikkeli-sivu yhteenveto 
  * Artikkelit, joilla ei ole tietyn sijainnin sivuja 
  * Artikkelit, joilla on tietyn sijainnin sivuja, mutta ei sisältöä näillä sivuilla 
  * Artikkelit, joilla on tietyn sijainnin sivuja, joilla on sisältöä 

![Locale usage overview](readme-images/locale-usage-overview.png)

---

### Julkaisut

Versioinnin ja julkaisujen avulla on mahdollista seurata sisältösi hirstoriaa. Kun teet Stencilissä julkaisun luot kuvan **koko** sisältösi tilasta sillä hetkellä. Tällöin on mahdollista ottaa julkaisu käyttöön joko testinä tai tuotannon julkaisuna.   

Jos julkaisu osoittautuu erilaiseksi kuin odotit, se on helposti palautettavissa aikaisemman julkaisun tilaan. 

---


## Stencil-käyttöliittymän esittely

![Stencil UI](readme-images/stencil-ui.png)  

### 1: Explorer

Käytä Explorer-ominaisuutta katsellaksesi artikkeleita ja niihin liitettyjä ominaisuuksia.

### 2: Työkalupalkki

Työkalupalkki sisältää pikakuvakelinkkejä päivittäisiin toimintoihin:

* Hallintatyökalu
* Artikkelinäkymä
* Linkkinäkymä
* Työnkulkunäkymä
* Julkaisunäkymä
* Sijaintinäkymä

#### Hallintatyökalu

Hallintatyökalu (työkalupalkin kotikuvake) on keskeinen paikka, josta luodaan ja muokataan resursseja. 

### 3: Artikkelivalinnat

Suurenna artikkelia työkalupalkissa ja klikkaa kolmen pisteen kuvaketta avataksesi artikkelivalikon. Täältä on mahdollista työstää artikkeliresursseja.

### 4: Välilehtivalikko

Katsele kaikkia aktiivisia resursseja ja vaihtele niiden välillä helposti.

### 5: Sisällönmuokkaus

Muokkaa sisältöäsi täällä. Ylläolevassa kuvassa on aktivoitu kaksoisnäkymä, mikä mahdollistaa kahden kielen muokkaamisen vierekkäin. Tätä voi hyödyntää erityisesti kääntäessä kieleltä toiselle. 

**HUOM**: Kaksoisnäkymä on käytettävissä vain artikkeleissa, joissa on KAKSI TAI USEAMPI sivu. Ottaaksesi kaksoisnäkymän käyttöön: 

1. Valitse sivu ja avaa se sisällönmuokkauksessa
2. Klikkaa kaksoisnäkymäpainiketta
3. Klikkaa toista sivua, jonka haluat avata sisällönmuokkauksessa
4. Sulkeaksesi toisen sivun klikkaa kaksoisnäkymäpainiketta uudelleen deaktivoidaksesi sen

---

## Ensimmäisten resurssien luominen 


#### Luo artikkeli

Valitse Explorerissa "Luo artikkeli". (Huom: tämä painike ilmestyy vain lisätessä ensimmäistä artikkelia). TAI

Mene hallintatyökaluun (kotikuvake työkalupalkissa) ja luo uusi artikkeli. 

Dialogi-ikkunassa jätä "Emoartikkeli" tyhjäksi, sillä et ole ottanut vielä muita artikkeleita käyttöösi.

Aseta artikkelisi järjestysluku. Järjestysluku määrittää artikkelin paikan portaalisi valikossa.

Luvun tulee olla kolminumeroinen. Mitä pienempi luku, sitä korkeammalle artikkeli ilmestyy valikossa. Esimerkiksi artikkeli, jonka järjestysluku on 100 on valikossa *korkeammalla* kuin artikkeli, jonka järjestysluku on 300.

Painettuasi "Luo" ensimmäinen artikkelisi luodaan Exploreriin. Et ole kuitenkaan luonut vielä sijainteja, joten sinulla ei myöskään ole vielä sivuja. Suurenna artikkelisi klikkaamalla laajennus-/nuolikuvaketta, ja järjestelmä ehdottaa ensimmäisen sijainnin luomista.

#### Luo sijainti

Näet ilmoituksen: "Ei sijainteja". Paina tästä lisätäksesi ensimmäisen, jotta voit alkaa luomaan sisältöä. 

Kun luot sijaintia:

Käytä kaksikirjaimista kielitunnistetta sen mukaan, minkä kielisen sijainnin haluat luoda. Esim:  

`en` : englanti
`fi` : suomi  
`sv` : ruotsi  

#### Luo sivu sijainnille
 
Kun olet luonut sijainnin voit lisätä siihen sivun artikkelisi laajennetussa näkymässä klikkaamalla "Luo sivu". 
HUOM: Kun olet luonut kaksi tai useamman sivua voit aktivoida kaksoisnäkymän. Tämä mahdollistaa siis rinnakkaisen sisällön tuottamisen kahdella eri kielellä. 

#### Lisää linkkejä, työnkulkuja ja ylimääräisiä sijainteja

Linkit, työnkulut ja ylimääräiset sijainnit lisätään globaalisti (koko domainiin). Näitä on mahdollista luoda milloin ja missä järjestyksessä tahansa hallintatyökalussa. 

#### Yhdistä linkit ja työnkulut artikkeleihin ja sivuihin 

Kun linkki / työnkulku on luotu, se voidaan yhdistää olemassaolevaan artikkeliin. Valitse artikkelivalikko, josta "Lisää ja poista artikkelilinkkejä" tai "Lisää ja poista artikkelien työnkulkuja". 

Linkille määrittämäsi sijainti luo automaattisen yhteyden linkin ja artikkelissa oikeankielisen sivun välille.

#### Luo julkaisu

* Kun sisältösi on valmista, luo uusi julkaisu. Nimeä se ja kirjoita mahdolliset lisätiedot ja julkaise. Voit ladata sivustosi JSON-tiedoston koska tahansa. 

## Resurssien poistaminen ja deaktivointi

**TÄRKEÄÄ HUOMIOITAVAA**:

**Poistaminen** on globaali eli koko sovelluksen kattava toiminto. Poistettu resurssi ei ole enää saatavilla. i
**Deaktivointi** koskee yhtä resurssia tai resurssiyhteyttä. Resurssi on deaktivoinnin jälkeen edelleen saatavilla koko sovelluksessa esim. uudelleenaktivointia varten, mutta ei näy enää käyttäjäpuolella. 
* **Artikkeli**: Artikkelit poistetaan globaalisti "Artikkelit"-valikosta.  
* **Sivu**: Koska sivut eivät voi olla olemassa ilman artikkelia, ne poistetaan artikkelistaan "Artikkelivalinnat"-valikosta. 

Huomioithan, että artikkelia poistaessa myös **kaikki sen sisältämät sivut** poistuvat. 

* **Linkki**: Linkit poistetaan globaalisti linkkinäkymästä. 
 
Linkki-/artikkeliyhteydet deaktivoidaan "Lisää ja poista artikkelilinkkejä" -kohdasta artikkelivalikosta.  

* **Työnkulku**: Työnkulut poistetaan globaalisti työnkulkunäkymästä.

Työnkulku-/artikkeliyhteydet deaktivoidaan "Lisää ja poista artikkelien työnkulkuja" -kohdasta artikkelivalikosta. 


* **Sijainti**: Sijainnit poistetaan globaalisti "Sijainnit" -kohdasta.

Sijainteja voidaan hallita "Aktivoi" ja "Deaktivoi" valinnoilla. Jos sijainti on deaktivoitu kaikki sen sijainnin resurssit poistuvat käyttäjänäkymästä. Tämä tehdään sijaintinäkymästä. 


* **Julkaisu**: Julkaisuja ei voida poistaa tai muokata niiden tekemisen jälkeen.

Aikaisempia julkaisuja voidaan aktivoida, jos on tarpeellista palata aikaisempaan versioon ja niitä on mahdollista tarkastella muutenkin.  



