package com.example.ngilizcequizapp

data class ReadingStory(
    val title: String,
    val level: String,
    val content: String,
    val questions: List<ReadingQuizQuestion>
)

object ReadingDataRepository {

    // ==========================================
    // A1 SEVİYESİ HİKAYELERİ (30 ADET)
    // ==========================================
    val a1Stories: List<ReadingStory> = listOf(
        ReadingStory(
            title = "My Pet Dog",
            level = "A1",
            content = """
                I have a small dog. His name is Max. Max is white and brown. 
                He has big ears and a happy tail. Every morning, Max runs in the garden. 
                He loves eating biscuits and drinking cold water. Max is my best friend.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Köpeğin adı nedir?", "Max", listOf("Max", "Leo", "Charlie", "Rocky")),
                ReadingQuizQuestion("Max her sabah nerede koşar?", "Bahçede", listOf("Bahçede", "Parkta", "Odada", "Sokakta")),
                ReadingQuizQuestion("Max ne yemeyi çok sever?", "Bisküvi", listOf("Bisküvi", "Elma", "Ekmek", "Peynir"))
            )
        ),
        ReadingStory(
            title = "The City Zoo",
            level = "A1",
            content = """
                Today, we are at the city zoo. There are many wild animals here. 
                I see a tall giraffe. It eats green leaves from a tree. 
                Next to the giraffe, two monkeys play together. They are very funny. 
                I take photos with my camera.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Metinde geçen zürafa ne yiyor?", "Yeşil yapraklar", listOf("Yeşil yapraklar", "Muz", "Elma", "Bisküvi")),
                ReadingQuizQuestion("Zürafanın yanında hangi hayvanlar oynuyor?", "İki maymun", listOf("İki maymun", "Üç aslan", "Kuşlar", "Ayılar")),
                ReadingQuizQuestion("Yazar hayvanların fotoğrafını neyle çekiyor?", "Kamerasıyla", listOf("Kamerasıyla", "Akıllı telefonuyla", "Dijital tabletiyle", "Fotoğraf makinesiyle"))
            )
        ),
        ReadingStory(
            title = "In the Classroom",
            level = "A1",
            content = """
                Our classroom is bright and clean. There are twenty desks and a big whiteboard. 
                Mr. Brown is our English teacher. He writes new words on the board. 
                We open our notebooks and listen carefully. I sit near the window with my friend David.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("İngilizce öğretmeninin adı nedir?", "Mr. Brown", listOf("Mr. Brown", "Mr. Smith", "Mr. Green", "Mr. White")),
                ReadingQuizQuestion("Yazar sınıfta kiminle birlikte oturuyor?", "David", listOf("David", "Ali", "Alex", "Tom")),
                ReadingQuizQuestion("Sınıfta kaç adet öğrenci sırası vardır?", "Yirmi", listOf("Yirmi", "On beş", "Otuz", "On"))
            )
        ),
        ReadingStory(
            title = "My Morning Breakfast",
            level = "A1",
            content = """
                Breakfast is my favorite meal. Every day, I wake up hungry. 
                I eat two boiled eggs and fresh cheese. My father brings warm bread from the bakery. 
                I drink orange juice, but my parents drink hot tea. 
                Breakfast gives me great energy for school.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar kahvaltıda ne içer?", "Portakal suyu", listOf("Portakal suyu", "Sıcak çay", "Süt", "Kahve")),
                ReadingQuizQuestion("Yazar kahvaltıda kaç adet yumurta yer?", "İki adet", listOf("İki adet", "Bir adet", "Üç adet", "Dört adet")),
                ReadingQuizQuestion("Sıcak ekmeği fırından kim getirir?", "Babası", listOf("Babası", "Annesi", "Arkadaşı", "Kendisi"))
            )
        ),
        ReadingStory(
            title = "My Bedroom",
            level = "A1",
            content = """
                My bedroom is my private space. The walls are light blue. 
                There is a comfortable bed and a wooden desk. 
                On my desk, there is a reading lamp and three notebooks. 
                A colorful carpet is on the floor. I like reading books in my room before I sleep.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Odanın duvarları ne renktir?", "Açık mavi", listOf("Açık mavi", "Beyaz", "Sarı", "Açık yeşil")),
                ReadingQuizQuestion("Masanın üzerinde ne vardır?", "Okuma lambası ve defterler", listOf("Okuma lambası ve defterler", "Bilgisayar", "Televizyon", "Çanta")),
                ReadingQuizQuestion("Yazar uyumadan önce odasında ne yapmayı sever?", "Kitap okumayı", listOf("Kitap okumayı", "Müzik dinlemeyi", "Resim çizmeyi", "Oyun oynamayı"))
            )
        ),
        ReadingStory(
            title = "A Rainy Day",
            level = "A1",
            content = """
                It is raining outside today. Dark clouds cover the sky. 
                I wear my yellow raincoat and boots. I hold a blue umbrella in my hand. 
                Water drops fall on the street. I walk to the small café and order a cup of hot chocolate.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar hangi renk yağmurluk giyiyor?", "Sarı", listOf("Sarı", "Mavi", "Kırmızı", "Siyah")),
                ReadingQuizQuestion("Yazarın elindeki şemsiye ne renktir?", "Mavi", listOf("Mavi", "Sarı", "Yeşil", "Beyaz")),
                ReadingQuizQuestion("Yazar kafede ne sipariş eder?", "Sıcak çikolata", listOf("Sıcak çikolata", "Soğuk su", "Çay", "Portakal suyu"))
            )
        ),
        ReadingStory(
            title = "Buying Clothes",
            level = "A1",
            content = """
                I need a new shirt for my school party. I go to the clothing store with my mother. 
                There are shirts in many colors. I choose a red cotton shirt. It is fifty liras. 
                My mother buys it for me. I am very happy with my new shirt.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar hangi kıyafeti seçiyor?", "Kırmızı pamuklu gömlek", listOf("Kırmızı pamuklu gömlek", "Mavi ceket", "Siyah pantolon", "Beyaz tişört")),
                ReadingQuizQuestion("Yazar mağazaya kiminle gider?", "Annesiyle", listOf("Annesiyle", "Babasıyla", "Arkadaşıyla", "Yalnız")),
                ReadingQuizQuestion("Gömleğin fiyatı ne kadardır?", "Elli lira", listOf("Elli lira", "Yüz lira", "Yirmi lira", "Kırk lira"))
            )
        ),
        ReadingStory(
            title = "At the Park",
            level = "A1",
            content = """
                The sun is shining today. Children are playing at the neighborhood park. 
                Some boys ride their bicycles. Two girls swing high in the air. 
                A little boy feeds the birds with bread crumbs. The grass is very green and fresh.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Parktaki küçük çocuk kuşlara ne veriyor?", "Ekmek kırıntıları", listOf("Ekmek kırıntıları", "Mısır", "Su", "Bisküvi")),
                ReadingQuizQuestion("Kızlar parkta ne yapıyor?", "Salıncakta sallanıyor", listOf("Salıncakta sallanıyor", "Bisiklete biniyor", "Koşuyor", "Top oynuyor")),
                ReadingQuizQuestion("Parktaki çimenler nasıldır?", "Çok yeşil ve taze", listOf("Çok yeşil ve taze", "Sarı ve kuru", "Islak ve çamurlu", "Kısa"))
            )
        ),
        ReadingStory(
            title = "My Favorite Colors",
            level = "A1",
            content = """
                Colors make our world wonderful. My favorite color is green because it reminds me of nature and trees. 
                My sister loves pink, and she has a pink school bag. Blue is the color of the sea and the sky. 
                Everything looks alive with colors.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın en sevdiği renk hangisidir?", "Yeşil", listOf("Yeşil", "Mavi", "Pembe", "Sarı")),
                ReadingQuizQuestion("Yazarın kız kardeşinin okul çantası ne renktir?", "Pembe", listOf("Pembe", "Yeşil", "Mavi", "Kırmızı")),
                ReadingQuizQuestion("Mavi renk yazara neyi hatırlatır?", "Denizi ve gökyüzünü", listOf("Denizi ve gökyüzünü", "Ağaçları", "Çiçekleri", "Güneşi"))
            )
        ),
        ReadingStory(
            title = "A Visit to Grandmother",
            level = "A1",
            content = """
                On Sundays, I visit my grandmother. She lives in a cute stone house. 
                She has a big garden with red roses. My grandmother bakes an apple pie for me. 
                We drink tea together and talk about my school. I love her warm smiles.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Büyükanne nasıl bir evde yaşıyor?", "Taş evde", listOf("Taş evde", "Büyük apartmanda", "Ahşap kulübede", "Çiftlikte")),
                ReadingQuizQuestion("Büyükanne yazar için ne pişirir?", "Elma turtası", listOf("Elma turtası", "Kek", "Börek", "Kurabiye")),
                ReadingQuizQuestion("Bahçede hangi çiçekler vardır?", "Kırmızı güller", listOf("Kırmızı güller", "Sarı papatyalar", "Laleler", "Menekşeler"))
            )
        ),
        ReadingStory(
            title = "Fruits and Vegetables",
            level = "A1",
            content = """
                Healthy food is very important for our bodies. Apples and oranges are sweet fruits. 
                They have vitamin C. Carrots and spinach are fresh vegetables. 
                My mother makes vegetable soup every Monday. I eat fruits after lunch.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar meyveleri ne zaman yer?", "Öğle yemeğinden sonra", listOf("Öğle yemeğinden sonra", "Sabah erken", "Uyumadan önce", "Akşam yemeğinden önce")),
                ReadingQuizQuestion("Anne her Pazartesi ne çorbası yapar?", "Sebze çorbası", listOf("Sebze çorbası", "Tavuk çorbası", "Mercimek çorbası", "Domates çorbası")),
                ReadingQuizQuestion("Elma ve portakalda hangi vitamin vardır?", "C vitamini", listOf("C vitamini", "A vitamini", "D vitamini", "B vitamini"))
            )
        ),
        ReadingStory(
            title = "The Little Kitten",
            level = "A1",
            content = """
                There is a small kitten in our backyard. Her fur is gray and soft. 
                Her eyes are bright green. She is hungry, so I give her some warm milk in a bowl. 
                She drinks it quickly and sleeps under the big tree. She feels safe now.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yavru kedinin tüyleri ne renktir?", "Gri", listOf("Gri", "Beyaz", "Siyah", "Sarı")),
                ReadingQuizQuestion("Yazar yavru kediye ne verir?", "Ilık süt", listOf("Ilık süt", "Su", "Kuru mama", "Balık")),
                ReadingQuizQuestion("Kedi sütü içtikten sonra nerede uyur?", "Büyük ağacın altında", listOf("Büyük ağacın altında", "Sepette", "Koltukta", "Kapının önünde"))
            )
        ),
        ReadingStory(
            title = "At the Bakery",
            level = "A1",
            content = """
                The bakery on our street smells delicious. The baker wakes up at five o'clock every morning. 
                He makes fresh bread, cheese rolls, and cookies. I buy two loaves of bread for my family. 
                The baker always smiles and says 'Have a nice day!'
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Fırıncı her sabah saat kaçta uyanır?", "Saat beşte", listOf("Saat beşte", "Saat yedide", "Saat altıda", "Saat sekizde")),
                ReadingQuizQuestion("Yazar ailesi için ne satın alır?", "İki somun ekmek", listOf("İki somun ekmek", "Kurabiye", "Peynirli poğaça", "Simit")),
                ReadingQuizQuestion("Fırıncı müşterilere ne söyler?", "'Have a nice day!' (İyi günler!)", listOf("'Have a nice day!' (İyi günler!)", "'Welcome!'", "'Good night!'", "'Goodbye!'"))
            )
        ),
        ReadingStory(
            title = "My Best Friend",
            level = "A1",
            content = """
                My best friend's name is Leo. He is ten years old, just like me. 
                Leo is tall with curly brown hair. We are in the same class at school. 
                During break time, we play football in the yard. Leo is very kind and shares his pencils.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Leo kaç yaşındadır?", "On yaşında", listOf("On yaşında", "Sekiz yaşında", "On iki yaşında", "Dokuz yaşında")),
                ReadingQuizQuestion("Teneffüste okul bahçesinde ne oynarlar?", "Futbol", listOf("Futbol", "Basketbol", "Saklambaç", "Koşu")),
                ReadingQuizQuestion("Leo diğer öğrencilerle neyini paylaşır?", "Kalemlerini", listOf("Kalemlerini", "Kitaplarını", "Yemeğini", "Oyuncaklarını"))
            )
        ),
        ReadingStory(
            title = "A Day at the Beach",
            level = "A1",
            content = """
                The summer holiday is here. My family and I are at the beach. 
                The sand is yellow and warm under our feet. My sister builds a big sandcastle. 
                I swim in the blue sea with my father. We eat cold watermelon under a large umbrella.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Kız kardeş sahilde ne yapıyor?", "Kumdan kale inşa ediyor", listOf("Kumdan kale inşa ediyor", "Yüzüyor", "Güneşleniyor", "Top oynuyor")),
                ReadingQuizQuestion("Şemsiyenin altında ne yerler?", "Soğuk karpuz", listOf("Soğuk karpuz", "Dondurma", "Sandviç", "Elma")),
                ReadingQuizQuestion("Sahildeki kumlar nasıldır?", "Sarı ve sıcak", listOf("Sarı ve sıcak", "Islak ve soğuk", "Beyaz ve sert", "Taşlı"))
            )
        ),
        ReadingStory(
            title = "My School Bag",
            level = "A1",
            content = """
                I have a heavy school bag today. Inside, there are five textbooks and a math notebook. 
                My pencil case is full of colored pencils and a green eraser. 
                I also carry a bottle of water and an apple. I am ready for my lessons.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Çantadaki silgi ne renktir?", "Yeşil", listOf("Yeşil", "Mavi", "Beyaz", "Kırmızı")),
                ReadingQuizQuestion("Çantada kaç adet ders kitabı vardır?", "Beş adet", listOf("Beş adet", "Üç adet", "Dört adet", "Altı adet")),
                ReadingQuizQuestion("Yazar çantasında meyve olarak ne taşır?", "Elma", listOf("Elma", "Muz", "Portakal", "Çilek"))
            )
        ),
        ReadingStory(
            title = "Winter Snow",
            level = "A1",
            content = """
                It is winter and the ground is covered in white snow. My brother and I go outside. 
                We make a big snowman in the front yard. We put a red scarf around his neck and a carrot for his nose. 
                It is very cold, but we have great fun.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Kardan adamın burnu için ne kullanılır?", "Havuç", listOf("Havuç", "Kömür", "Taş", "Elma")),
                ReadingQuizQuestion("Kardan adamın boynuna ne takarlar?", "Kırmızı atkı", listOf("Kırmızı atkı", "Mavi bere", "Eldiven", "Ceket")),
                ReadingQuizQuestion("Kardan adamı nerede yaparlar?", "Ön bahçede", listOf("Ön bahçede", "Parkta", "Sokakta", "Arka bahçede"))
            )
        ),
        ReadingStory(
            title = "The Big Clock",
            level = "A1",
            content = """
                There is an old clock on the living room wall. It makes a ticking sound every second. 
                At seven in the morning, the alarm rings. My mother says time is precious. 
                The clock helps us get ready for school and work on time.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Saat evin neresindedir?", "Oturma odasının duvarında", listOf("Oturma odasının duvarında", "Mutfakta", "Yatak odasında", "Girişte")),
                ReadingQuizQuestion("Alarm sabah saat kaçta çalar?", "Yedide", listOf("Yedide", "Altıda", "Sekizde", "Dokuzda")),
                ReadingQuizQuestion("Saat aileye ne konuda yardımcı olur?", "Zamanında hazır olmakta", listOf("Zamanında hazır olmakta", "Uyumakta", "Ders çalışmakta", "Yemek yemekte"))
            )
        ),
        ReadingStory(
            title = "In the Kitchen",
            level = "A1",
            content = """
                The kitchen is warm and full of life. My mother is preparing dinner. 
                There are red tomatoes and cucumbers on the counter. A silver pot is on the stove with boiling soup. 
                I help set the dining table with spoons and plates.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar sofrayı neyle hazırlar?", "Kaşıklar ve tabaklarla", listOf("Kaşıklar ve tabaklarla", "Bardaklar ve çatallarla", "Peçetelerle", "Ekmekle")),
                ReadingQuizQuestion("Ocakta ne kaynıyor?", "Çorba", listOf("Çorba", "Su", "Çay", "Süt")),
                ReadingQuizQuestion("Tezgahta hangi sebzeler vardır?", "Domates ve salatalık", listOf("Domates ve salatalık", "Biber ve soğan", "Patates ve havuç", "Marul"))
            )
        ),
        ReadingStory(
            title = "A Cup of Hot Tea",
            level = "A1",
            content = """
                It is a chilly evening. I sit on the sofa with a blanket. 
                My sister brings me a cup of hot black tea. There is a slice of lemon in it. 
                The warm cup heats my cold hands. Drinking tea is very relaxing after a long day.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Çayın içinde ne vardır?", "Bir dilim limon", listOf("Bir dilim limon", "Şeker", "Nane", "Karanfil")),
                ReadingQuizQuestion("Yazar nereye oturmuştur?", "Battaniyeyle kanepeye", listOf("Battaniyeyle kanepeye", "Sandalyeye", "Yatağa", "Halıya")),
                ReadingQuizQuestion("Çayı yazara kim getirir?", "Kız kardeşi", listOf("Kız kardeşi", "Annesi", "Babası", "Arkadaşı"))
            )
        ),
        ReadingStory(
            title = "My Little Bicycle",
            level = "A1",
            content = """
                I have a new red bicycle. It has shiny silver wheels and a loud bell. 
                I always wear my black helmet before riding it. In the afternoon, I ride in the quiet lane near my house. 
                Riding my bike makes me feel fast and happy.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Bisiklet ne renktir?", "Kırmızı", listOf("Kırmızı", "Mavi", "Siyah", "Yeşil")),
                ReadingQuizQuestion("Yazar bisiklete binmeden önce ne takar?", "Siyah kask", listOf("Siyah kask", "Eldiven", "Gözlük", "Dizlik")),
                ReadingQuizQuestion("Bisikletin zili nasıldır?", "Yüksek sesli", listOf("Yüksek sesli", "Kısık sesli", "Kırık", "Eski"))
            )
        ),
        ReadingStory(
            title = "The Friendly Neighbor",
            level = "A1",
            content = """
                Mr. Adams is our next-door neighbor. He is an elderly man with silver hair. 
                He has a small garden with sweet strawberries. Every morning, he waters his plants and waves hello to everyone. 
                He often gives fresh strawberries to the neighborhood children.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Komşu Mr. Adams'ın bahçesinde ne yetişir?", "Çilekler", listOf("Çilekler", "Elmalar", "Güller", "Domatesler")),
                ReadingQuizQuestion("Mr. Adams her sabah ne yapar?", "Bitkilerini sular ve selam verir", listOf("Bitkilerini sular ve selam verir", "Gazete okur", "Yürüyüşe çıkar", "Araba yıkar")),
                ReadingQuizQuestion("Mr. Adams'ın saçları ne renktir?", "Gümüş / Gri", listOf("Gümüş / Gri", "Siyah", "Sarı", "Kahverengi"))
            )
        ),
        ReadingStory(
            title = "At the Toy Store",
            level = "A1",
            content = """
                Saturday is shopping day. Today, I visit the toy store in the city center. 
                The shelves are full of colorful puzzles, toy cars, and teddy bears. 
                I like the blue race car on the top shelf. It has four big black wheels.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar üst raftaki hangi oyuncağı beğenir?", "Mavi yarış arabasını", listOf("Mavi yarış arabasını", "Ayıcığı", "Yapbozu", "Uçağı")),
                ReadingQuizQuestion("Yarış arabasının tekerlekleri ne renktir?", "Siyah", listOf("Siyah", "Kırmızı", "Sarı", "Gümüş")),
                ReadingQuizQuestion("Oyuncakçı dükkanı nerededir?", "Şehir merkezinde", listOf("Şehir merkezinde", "Okulun yanında", "Mahallede", "Parkın karşısında"))
            )
        ),
        ReadingStory(
            title = "Our Living Room",
            level = "A1",
            content = """
                Our living room is large and comfortable. There is a brown sofa and two green armchairs. 
                A flat television hangs on the wall. In the evening, our family gathers here to watch a movie and talk about our day. 
                It is our favorite family room.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Oturma odasındaki kanepe ne renktir?", "Kahverengi", listOf("Kahverengi", "Yeşil", "Mavi", "Beyaz")),
                ReadingQuizQuestion("Aile akşamları odada ne yapar?", "Film izler ve sohbet eder", listOf("Film izler ve sohbet eder", "Yemek yer", "Ders çalışır", "Müzik çalar")),
                ReadingQuizQuestion("Tekli koltuklar ne renktir?", "Yeşil", listOf("Yeşil", "Kahverengi", "Sarı", "Gri"))
            )
        ),
        ReadingStory(
            title = "My New Shoes",
            level = "A1",
            content = """
                My old shoes are too small now. Yesterday, my father bought me white sports shoes. 
                They are light and very comfortable. I practice tying the shoelaces myself. 
                Today, I run fast in the school yard with my new shoes.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yeni ayakkabılar ne renktir?", "Beyaz", listOf("Beyaz", "Siyah", "Mavi", "Kırmızı")),
                ReadingQuizQuestion("Ayakkabıları kim satın aldı?", "Babası", listOf("Babası", "Annesi", "Büyükbabası", "Kendisi")),
                ReadingQuizQuestion("Eski ayakkabılara ne olmuştu?", "Çok küçük geliyordu", listOf("Çok küçük geliyordu", "Yırtılmıştı", "Kirlenmişti", "Kaybolmuştu"))
            )
        ),
        ReadingStory(
            title = "Sunday Morning",
            level = "A1",
            content = """
                Sunday is my favorite day of the week. There is no school, so I don't set an alarm. 
                The morning is quiet and peaceful. The sun shines warmly through the kitchen window. 
                We have a long, relaxed breakfast with honey and pancakes.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Kahvaltıda ne yenir?", "Bal ve krep", listOf("Bal ve krep", "Yumurta ve peynir", "Tost", "Meyve salatası")),
                ReadingQuizQuestion("Pazar sabahı neden alarm kurulmaz?", "Okul olmadığı için", listOf("Okul olmadığı için", "Saat bozuk olduğu için", "Geç yatıldığı için", "Hafta sonu dinlenme günü olduğu için")),
                ReadingQuizQuestion("Sabah vakti nasıldır?", "Sessiz ve huzurlu", listOf("Sessiz ve huzurlu", "Gürültülü", "Soğuk ve yağmurlu", "Hızlı ve telaşlı"))
            )
        ),
        ReadingStory(
            title = "At the Ice Cream Shop",
            level = "A1",
            content = """
                It is a hot summer afternoon. Many people are in front of the ice cream shop. 
                There are strawberry, chocolate, and vanilla flavors. I choose one scoop of chocolate and one scoop of vanilla in a crispy cone. 
                It tastes sweet and cold.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar hangi dondurma çeşitlerini seçer?", "Çikolata ve vanilya", listOf("Çikolata ve vanilya", "Çilek ve çikolata", "Muz ve vanilya", "Limon ve çilek")),
                ReadingQuizQuestion("Yazar dondurmayı neyin içinde alır?", "Çıtır külah içinde", listOf("Çıtır külah içinde", "Bardakta", "Kasede", "Tabağında")),
                ReadingQuizQuestion("Dondurma dükkanına ne zaman gidilmiştir?", "Sıcak bir yaz öğleden sonrasında", listOf("Sıcak bir yaz öğleden sonrasında", "Kışın", "Sabah erkenden", "Gece geç saatte"))
            )
        ),
        ReadingStory(
            title = "My Pencil Case",
            level = "A1",
            content = """
                I have a neat pencil case with a metal zipper. Inside, there are three blue pens, two pencils, and a red sharpener. 
                Everything has its own place. My friends sometimes borrow my sharpener. 
                Keeping my pencil case organized helps me study.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Kalemtıraş ne renktir?", "Kırmızı", listOf("Kırmızı", "Mavi", "Yeşil", "Sarı")),
                ReadingQuizQuestion("Kalemlikte kaç adet mavi tükenmez kalem vardır?", "Üç adet", listOf("Üç adet", "İki adet", "Bir adet", "Dört adet")),
                ReadingQuizQuestion("Arkadaşları bazen yazardan ne ödünç alır?", "Kalemtıraşını", listOf("Kalemtıraşını", "Mavi kalemini", "Silgisini", "Cetvelini"))
            )
        ),
        ReadingStory(
            title = "The Yellow School Bus",
            level = "A1",
            content = """
                Every morning at eight o'clock, the yellow school bus arrives at my corner. 
                Mr. Miller is our friendly driver. He always honks the horn twice. 
                I sit next to the window and wave goodbye to my mother. The ride to school is fun with my classmates.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Okul servisi sabah saat kaçta gelir?", "Saat sekizde", listOf("Saat sekizde", "Saat yedide", "Saat sekiz buçukta", "Saat dokuzda")),
                ReadingQuizQuestion("Servis şoförünün adı nedir?", "Mr. Miller", listOf("Mr. Miller", "Mr. Brown", "Mr. Adams", "Mr. Smith")),
                ReadingQuizQuestion("Şoför kornaya kaç kez basar?", "İki kez", listOf("İki kez", "Bir kez", "Üç kez", "Hiç basmaz"))
            )
        ),
        ReadingStory(
            title = "Bedtime Story",
            level = "A1",
            content = """
                It is nine o'clock at night. I brush my teeth and put on my blue pajamas. 
                I climb into my soft bed. My father comes into my room and reads a short story about brave sailors. 
                I listen with my eyes closed, and soon I fall asleep happily.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Babanın okuduğu hikaye ne hakkındadır?", "Cesur denizciler", listOf("Cesur denizciler", "Ormandaki hayvanlar", "Uzay yolculuğu", "Büyük bir kedi")),
                ReadingQuizQuestion("Yazarın pijamaları ne renktir?", "Mavi", listOf("Mavi", "Beyaz", "Sarı", "Kırmızı")),
                ReadingQuizQuestion("Yazar saat kaçta yatağa girer?", "Saat dokuzda", listOf("Saat dokuzda", "Saat onda", "Saat sekizde", "Saat on birde"))
            )
        )
    )

    // ==========================================
    // A2 SEVİYESİ HİKAYELERİ (30 ADET)
    // ==========================================
    val a2Stories: List<ReadingStory> = listOf(
        ReadingStory(
            title = "Ordering at a Café",
            level = "A2",
            content = """
                Yesterday afternoon, my friend Sarah and I went to a cozy café downtown. 
                The waiter brought us the menu with many dessert options. 
                Sarah ordered a slice of carrot cake, and I wanted a cheese sandwich with fresh orange juice. 
                The food arrived quickly and was very delicious. 
                We paid fifteen euros and left a generous tip for the waiter.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Sarah kafede ne sipariş etti?", "Havuçlu kek dilimi", listOf("Havuçlu kek dilimi", "Peynirli sandviç", "Çikolatalı pasta", "Kruvasan")),
                ReadingQuizQuestion("Yazar içecek olarak ne istedi?", "Taze portakal suyu", listOf("Taze portakal suyu", "Sıcak çay", "Kahve", "Limonata")),
                ReadingQuizQuestion("Toplam hesap kaç euro tuttu?", "On beş euro", listOf("On beş euro", "Yirmi euro", "On euro", "Yirmi beş euro"))
            )
        ),
        ReadingStory(
            title = "A Weekend Camping Trip",
            level = "A2",
            content = """
                Last Saturday, my family decided to go camping near Lake Blue. 
                We packed our warm sleeping bags, a large green tent, and plenty of canned food. 
                When we arrived, my father and I set up the tent under two tall pine trees. 
                In the evening, we built a small campfire and roasted marshmallows while telling interesting stories.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Aile çadırı nereye kurdu?", "İki uzun çam ağacının altına", listOf("İki uzun çam ağacının altına", "Gölün tam kenarına", "Tepenin üstüne", "Arabanın yanına")),
                ReadingQuizQuestion("Akşam kamp ateşinde ne pişirdiler?", "Marşmelov", listOf("Marşmelov", "Balık", "Sosis", "Mısır")),
                ReadingQuizQuestion("Kampa hangi gün gittiler?", "Geçen Cumartesi", listOf("Geçen Cumartesi", "Geçen Pazar", "Cuma günü", "Pazartesi"))
            )
        ),
        ReadingStory(
            title = "Cooking Italian Pasta",
            level = "A2",
            content = """
                Last night, I cooked dinner for my parents for the first time. 
                I chose an Italian tomato pasta recipe from a cooking book. 
                First, I boiled water in a deep pot and added salt. 
                Then, I chopped fresh onions and tomatoes for the sauce. 
                My parents said the pasta was better than restaurant food, which made me feel very proud.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar sos için hangi sebzeleri doğradı?", "Taze soğan ve domates", listOf("Taze soğan ve domates", "Biber ve sarımsak", "Mantar ve havuç", "Patates ve soğan")),
                ReadingQuizQuestion("Tarifi nereden buldu?", "Yemek kitabından", listOf("Yemek kitabından", "İnternetten", "Annesinden", "Televizyondan")),
                ReadingQuizQuestion("Anne ve babası makarna hakkında ne söyledi?", "Restoran yemeğinden daha iyi olduğunu", listOf("Restoran yemeğinden daha iyi olduğunu", "Çok tuzlu olduğunu", "Sosu az bulduklarını", "Biraz daha pişmesi gerektiğini"))
            )
        ),
        ReadingStory(
            title = "My First Flight",
            level = "A2",
            content = """
                Two months ago, I traveled by plane for the first time. 
                I was nervous because I had never flown before. 
                At the airport, we checked our heavy suitcases and walked through security. 
                During take-off, the plane was fast and loud, but looking at the white clouds from the window was magical. 
                It was much easier and faster than taking the train.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar kalkış sırasında nasıl hissetti?", "İlk başta gergindi", listOf("İlk başta gergindi", "Çok uykuluydu", "Sıkılmıştı", "Korkusuzdu")),
                ReadingQuizQuestion("Yazar camdan baktığında ne gördü?", "Beyaz bulutları", listOf("Beyaz bulutları", "Denizi", "Şehir ışıklarını", "Dağları")),
                ReadingQuizQuestion("Uçak yolculuğu trene göre nasıldı?", "Çok daha kolay ve hızlıydı", listOf("Çok daha kolay ve hızlıydı", "Daha yavaş ama ucuzdu", "Daha yorucuydu", "Aynıydı"))
            )
        ),
        ReadingStory(
            title = "A Visit to the Dentist",
            level = "A2",
            content = """
                Last Tuesday, I had a toothache, so I made an appointment with Dr. Miller. 
                I was scared of the dental tools at first. 
                However, the dentist was very gentle and explained every step clearly. 
                He checked my teeth and told me that I should brush twice a day and eat fewer sweets. 
                My tooth stopped hurting, and I received a free blue toothbrush.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Diş hekimi yazara ne tavsiye etti?", "Günde iki kez fırçalamayı ve daha az tatlı yemeyi", listOf("Günde iki kez fırçalamayı ve daha az tatlı yemeyi", "Sadece su içmeyi", "Hiç sakız çiğnememeyi", "Hergün kontrole gelmeyi")),
                ReadingQuizQuestion("Diş hekimi yazara hediye olarak ne verdi?", "Mavi diş fırçası", listOf("Mavi diş fırçası", "Diş macunu", "Etiket", "Ayna")),
                ReadingQuizQuestion("Randevu hangi gündü?", "Geçen Salı", listOf("Geçen Salı", "Geçen Çarşamba", "Pazartesi", "Cuma"))
            )
        ),
        ReadingStory(
            title = "Lost at the Train Station",
            level = "A2",
            content = """
                When I arrived in Manchester last weekend, the train station was crowded and confusing. 
                I had to catch Platform 4, but I walked towards Platform 9 by mistake. 
                A friendly station officer noticed that I looked lost. 
                He checked my ticket and guided me to the correct platform just two minutes before the train departed.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar hangi şehirdeki tren istasyonundaydı?", "Manchester", listOf("Manchester", "Londra", "Liverpool", "Oxford")),
                ReadingQuizQuestion("Yazarın gitmesi gereken doğru peron hangisiydi?", "Peron 4", listOf("Peron 4", "Peron 9", "Peron 2", "Peron 7")),
                ReadingQuizQuestion("Yazara peronu bulmasında kim yardımcı oldu?", "İstasyon görevlisi", listOf("İstasyon görevlisi", "Bir yolcu", "Bilet satıcısı", "Arkadaşı"))
            )
        ),
        ReadingStory(
            title = "Planning a Surprise Party",
            level = "A2",
            content = """
                Next Friday is my sister's birthday, so my family is planning a secret party for her. 
                My mother ordered a chocolate strawberry cake from the bakery. 
                My father bought colorful balloons and party hats. 
                I invited ten of her closest school friends, and everyone promised to keep the secret. 
                She thinks we are just having a normal dinner.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Sürpriz parti kimin için planlanıyor?", "Yazarın kız kardeşi için", listOf("Yazarın kız kardeşi için", "Annesi için", "Babası için", "En yakın arkadaşı için")),
                ReadingQuizQuestion("Parti için kaç arkadaş davet edildi?", "On yakın arkadaş", listOf("On yakın arkadaş", "Beş arkadaş", "On beş arkadaş", "Yirmi arkadaş")),
                ReadingQuizQuestion("Kız kardeş o akşam ne olacağını sanıyor?", "Normal bir akşam yemeği yiyeceklerini", listOf("Normal bir akşam yemeği yiyeceklerini", "Sinemaya gideceklerini", "Ders çalışacaklarını", "Parti olduğunu"))
            )
        ),
        ReadingStory(
            title = "Shopping at the Mall",
            level = "A2",
            content = """
                Yesterday was the seasonal discount at the city shopping mall. 
                My cousin and I went there early in the morning to avoid long queues. 
                I tried on a dark blue winter coat that was on sale. 
                It was warmer and cheaper than the coat in the other store. 
                My cousin bought a leather backpack, and we had burgers at the food court.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar alışveriş merkezinde ne denedi ve aldı?", "Koyu mavi kışlık kaban", listOf("Koyu mavi kışlık kaban", "Deri ceket", "Siyah bot", "Kırmızı kazak")),
                ReadingQuizQuestion("Kuzeni ne satın aldı?", "Deri sırt çantası", listOf("Deri sırt çantası", "Spor ayakkabı", "Saat", "Gömlek")),
                ReadingQuizQuestion("Neden sabah erkenden gittiler?", "Uzun kuyruklardan kaçınmak için", listOf("Uzun kuyruklardan kaçınmak için", "Mağaza erken kapandığı için", "Arkadaşlarıyla buluşmak için", "Hava yağmurlu olduğu için"))
            )
        ),
        ReadingStory(
            title = "My Favorite Sport",
            level = "A2",
            content = """
                I started playing basketball three years ago when my school built a new court. 
                In our team, I play as a guard because I can run fast and pass accurately. 
                We practice every Wednesday and Friday afternoon. 
                Last month, our school team won the regional trophy after beating our main rival team by two points.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar takımda hangi pozisyonda oynuyor?", "Guard (Oyun kurucu)", listOf("Guard (Oyun kurucu)", "Pivot", "Forvet", "Şutör gard")),
                ReadingQuizQuestion("Takım hangi günler antrenman yapıyor?", "Çarşamba ve Cuma öğleden sonra", listOf("Çarşamba ve Cuma öğleden sonra", "Pazartesi ve Salı", "Hafta sonları", "Her gün")),
                ReadingQuizQuestion("Geçen ayki maçta rakip takımı kaç sayı farkla yendiler?", "İki sayı farkla", listOf("İki sayı farkla", "Beş sayı farkla", "On sayı farkla", "Bir sayı farkla"))
            )
        ),
        ReadingStory(
            title = "A Day in My Hometown",
            level = "A2",
            content = """
                My hometown is a historic coastal town called Amasra. 
                Last summer, my foreign friend visited me there. 
                First, we walked across the Roman bridge and took pictures of the harbor. 
                Then, we ate fresh fish and a traditional salad with twenty different herbs. 
                My friend said it was the most peaceful place he had ever seen.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın memleketi hangi kasabadır?", "Amasra", listOf("Amasra", "Bodrum", "Çeşme", "Sinop")),
                ReadingQuizQuestion("Yazar ve arkadaşı hangi tarihi köprüden geçtiler?", "Roma köprüsünden", listOf("Roma köprüsünden", "Taş köprüden", "Ahşap köprüden", "Osmanlı köprüsünden")),
                ReadingQuizQuestion("Geleneksel salatanın içinde kaç çeşit ot/yeşillik vardı?", "Yirmi farklı çeşit", listOf("Yirmi farklı çeşit", "On çeşit", "Beş çeşit", "On beş çeşit"))
            )
        ),
        ReadingStory(
            title = "The School Festival",
            level = "A2",
            content = """
                Our school organized a cultural festival last Friday. 
                Students from different grades prepared food stalls, music shows, and science projects. 
                Our class set up a game booth where visitors threw balls into buckets to win prizes. 
                We collected over two hundred liras and donated all the money to the local children's hospital.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın sınıfı festivalde nasıl bir stant açtı?", "Kovalara top atma oyun standı", listOf("Kovalara top atma oyun standı", "Yemek standı", "Kitap satışı", "Bilim deneyi")),
                ReadingQuizQuestion("Toplanan para nereye bağışlandı?", "Yerel çocuk hastanesine", listOf("Yerel çocuk hastanesine", "Kütüphaneye", "Hayvan barınağına", "Okul bahçesine")),
                ReadingQuizQuestion("Festival hangi gün düzenlendi?", "Geçen Cuma", listOf("Geçen Cuma", "Geçen Cumartesi", "Pazartesi", "Perşembe"))
            )
        ),
        ReadingStory(
            title = "Moving to a New Apartment",
            level = "A2",
            content = """
                Moving houses was exhausting for our family last month. 
                We packed dozens of cardboard boxes with clothes and dishes. 
                Our new apartment is on the third floor of a modern building. 
                It is brighter and larger than our old flat. 
                Best of all, my new bedroom has a small balcony where I can see the city park.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yeni daire kaçıncı kattadır?", "Üçüncü katta", listOf("Üçüncü katta", "Birinci katta", "Beşinci katta", "Zemin katta")),
                ReadingQuizQuestion("Yeni daire eski daireye göre nasıldır?", "Daha aydınlık ve daha geniştir", listOf("Daha aydınlık ve daha geniştir", "Daha küçük ama ucuzdur", "Daha karanlıktır", "Eski ve gürültülüdür")),
                ReadingQuizQuestion("Yazarın yeni odasındaki en güzel özellik nedir?", "Şehir parkını gören küçük balkonu", listOf("Şehir parkını gören küçük balkonu", "Büyük dolabı", "Yeni yatağı", "Geniş masası"))
            )
        ),
        ReadingStory(
            title = "A Visit to the Science Museum",
            level = "A2",
            content = """
                Our science teacher took our class to the National Science Museum yesterday. 
                The museum had interactive rooms about space, electricity, and human biology. 
                My favorite section was the flight simulator where we could fly a digital airplane. 
                We learned that science can be fun and exciting outside of school textbooks.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Müzeye geziyi hangi dersin öğretmeni düzenledi?", "Fen / Bilim öğretmeni", listOf("Fen / Bilim öğretmeni", "Matematik öğretmeni", "Tarih öğretmeni", "İngilizce öğretmeni")),
                ReadingQuizQuestion("Yazarın müzedeki en sevdiği bölüm hangisiydi?", "Uçuş simülatörü", listOf("Uçuş simülatörü", "Elektrik odası", "Uzay bölümü", "Biyoloji laboratuvarı")),
                ReadingQuizQuestion("Öğrenciler müzeden nasıl bir ders çıkardı?", "Bilimin ders kitapları dışında da eğlenceli olabileceğini", listOf("Bilimin ders kitapları dışında da eğlenceli olabileceğini", "Müzelerin sıkıcı olduğunu", "Uçak kullanmanın zor olduğunu", "Daha çok çalışılması gerektiğini"))
            )
        ),
        ReadingStory(
            title = "My Part-Time Job",
            level = "A2",
            content = """
                During the summer holiday, I worked at the local bookstore on weekends. 
                My main duty was arranging books on the shelves according to their genre. 
                I also helped customers find history and English learning books. 
                Working there helped me improve my communication skills and earn my own pocket money.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar yaz tatilinde nerede çalıştı?", "Yerel kitapçıda", listOf("Yerel kitapçıda", "Kafede", "Markette", "Kütüphanede")),
                ReadingQuizQuestion("Yazarın ana görevi neydi?", "Kitapları türlerine göre raflara dizmek", listOf("Kitapları türlerine göre raflara dizmek", "Kasa işlemleri yapmak", "Kitap taşımak", "Temizlik yapmak")),
                ReadingQuizQuestion("Bu işte çalışmak yazara ne kazandırdı?", "İletişim becerilerini geliştirdi ve harçlık kazandırdı", listOf("İletişim becerilerini geliştirdi ve harçlık kazandırdı", "Daha hızlı okumayı öğretti", "Yeni kitaplar hediye edildi", "Yorulmasını engelledi"))
            )
        ),
        ReadingStory(
            title = "A Hike in the Mountains",
            level = "A2",
            content = """
                Early on Sunday morning, my hiking club traveled to the Green Valley mountains. 
                We carried backpacks with water bottles, energy bars, and a first-aid kit. 
                The trail was steep and rocky, but the fresh air gave us strength. 
                After three hours of climbing, we reached the summit and saw an unbelievable view of the valley below.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Tırmanış kaç saat sürdü?", "Üç saat", listOf("Üç saat", "İki saat", "Dört saat", "Bir saat")),
                ReadingQuizQuestion("Yürüyüş parkuru nasıldı?", "Dik ve kayalık", listOf("Dik ve kayalık", "Düz ve çimenlik", "Çamurlu ve ıslak", "Kısa ve kolay")),
                ReadingQuizQuestion("Zirveye ulaştıklarında ne gördüler?", "Vadinin inanılmaz manzarasını", listOf("Vadinin inanılmaz manzarasını", "Büyük bir göl", "Terk edilmiş bir kulübe", "Diğer dağcıları"))
            )
        ),
        ReadingStory(
            title = "Cooking with Grandma",
            level = "A2",
            content = """
                Whenever I stay with my grandmother, we spend Saturday mornings in the kitchen. 
                Yesterday, she taught me how to make traditional spinach pie from scratch. 
                She showed me how to roll the dough until it became paper-thin. 
                While it was baking in the oven, our whole house smelled wonderful.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Büyükanne yazara ne yapmayı öğretti?", "Geleneksel ıspanaklı börek", listOf("Geleneksel ıspanaklı börek", "Elmalı turta", "Ekmek", "Kurabiye")),
                ReadingQuizQuestion("Hamuru nasıl açmaları gerekiyordu?", "Kağıt inceliğinde olana kadar", listOf("Kağıt inceliğinde olana kadar", "Kalın ve yuvarlak", "Küçük parçalar halinde", "Hızlıca")),
                ReadingQuizQuestion("Börek nerede pişti?", "Fırında", listOf("Fırında", "Tavada", "Ocakta", "Izgarada"))
            )
        ),
        ReadingStory(
            title = "My Favorite Season",
            level = "A2",
            content = """
                Autumn is definitely my favorite time of the year. 
                The weather becomes cooler after the scorching summer heat. 
                Leaves turn golden, orange, and red before falling gently onto the pavement. 
                I love wearing my comfortable woolen sweaters and walking through the misty park with a hot coffee in hand.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın en sevdiği mevsim hangisidir?", "Sonbahar", listOf("Sonbahar", "İlkbahar", "Yaz", "Kış")),
                ReadingQuizQuestion("Ağaçların yaprakları hangi renklere döner?", "Altın sarısı, turuncu ve kırmızı", listOf("Altın sarısı, turuncu ve kırmızı", "Sarı ve yeşil", "Kahverengi ve siyah", "Sadece sarı")),
                ReadingQuizQuestion("Yazar parkta yürürken ne giymeyi sever?", "Yün kazaklarını", listOf("Yün kazaklarını", "Yağmurluğunu", "Deri ceketini", "Kabanını"))
            )
        ),
        ReadingStory(
            title = "The Botanical Garden",
            level = "A2",
            content = """
                Last weekend, our science club visited the city's botanical garden. 
                There was a huge glass greenhouse full of tropical flowers and towering palm trees. 
                A guide explained that some rare plants from the Amazon rainforest could not survive in cold climates. 
                We bought a miniature cactus at the gift shop as a souvenir.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Cam seranın içinde ne vardı?", "Tropik çiçekler ve palmiye ağaçları", listOf("Tropik çiçekler ve palmiye ağaçları", "Sebzeler ve meyveler", "Güller ve laleler", "Kuşlar")),
                ReadingQuizQuestion("Hediye dükkanından hatıra olarak ne satın aldılar?", "Minyatür bir kaktüs", listOf("Minyatür bir kaktüs", "Çiçek tohumu", "Kitap", "Kartpostal")),
                ReadingQuizQuestion("Rehber Amazon bitkileri hakkında ne anlattı?", "Soğuk iklimlerde yaşayamadıklarını", listOf("Soğuk iklimlerde yaşayamadıklarını", "Çok su istemediklerini", "Geceleri açtıklarını", "Zehirli olduklarını"))
            )
        ),
        ReadingStory(
            title = "Buying a Smartphone",
            level = "A2",
            content = """
                After saving money for six months, I finally bought my first smartphone yesterday. 
                In the electronics store, the clerk compared three different models for me. 
                I chose the silver phone because its camera was better and its battery lasted longer. 
                When I got home, I immediately called my grandmother to test the video call.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar telefon almak için ne kadar süre para biriktirdi?", "Altı ay", listOf("Altı ay", "Üç ay", "Bir yıl", "İki ay")),
                ReadingQuizQuestion("Yazar neden gümüş renkli telefonu seçti?", "Kamerası daha iyi ve bataryası daha uzun gittiği için", listOf("Kamerası daha iyi ve bataryası daha uzun gittiği için", "Daha ucuz olduğu için", "Daha küçük olduğu için", "Rengini çok sevdiği için")),
                ReadingQuizQuestion("Eve varınca görüntülü aramayla kimi aradı?", "Büyükannesini", listOf("Büyükannesini", "Annesini", "Arkadaşını", "Kuzenini"))
            )
        ),
        ReadingStory(
            title = "The Library Notebook",
            level = "A2",
            content = """
                While I was studying in the quiet university library, I noticed a brown leather notebook under the chair. 
                I opened it and saw detailed math formulas and a student name on the first page. 
                I took the notebook to the front desk officer. 
                An hour later, a relieved engineering student came and thanked me sincerely.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar sandalyenin altında ne buldu?", "Kahverengi deri bir not defteri", listOf("Kahverengi deri bir not defteri", "Bir ders kitabı", "Kalem kutusu", "Cüzdan")),
                ReadingQuizQuestion("Defterin içinde ne vardı?", "Ayrıntılı matematik formülleri", listOf("Ayrıntılı matematik formülleri", "Şiirler", "İngilizce kelimeler", "Tarih notları")),
                ReadingQuizQuestion("Defterin sahibi hangi bölümün öğrencisiydi?", "Mühendislik", listOf("Mühendislik", "Tıp", "Hukuk", "Mimarlık"))
            )
        ),
        ReadingStory(
            title = "A Movie Night at the Cinema",
            level = "A2",
            content = """
                My friends and I went to the cinema on Friday evening to watch an animated adventure movie. 
                We bought tickets online to choose the best seats in the center row. 
                We also bought a large tub of salted popcorn and cold drinks. 
                The animation was colorful and the funny characters made everyone laugh loudly.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Hangi tür film izlediler?", "Animasyon macera filmi", listOf("Animasyon macera filmi", "Korku filmi", "Bilim kurgu", "Komedi filmi")),
                ReadingQuizQuestion("Biletleri nereden aldılar?", "İnternet üzerinden (online)", listOf("İnternet üzerinden (online)", "Gişeden", "Arkadaşlarından", "Girişte")),
                ReadingQuizQuestion("Seyirciler film sırasında ne yaptı?", "Komik karakterlere yüksek sesle güldü", listOf("Komik karakterlere yüksek sesle güldü", "Korktu", "Sessizce oturdu", "Not aldı"))
            )
        ),
        ReadingStory(
            title = "Taking Care of Houseplants",
            level = "A2",
            content = """
                When my mother went on a business trip last week, she gave me the responsibility of caring for her plants. 
                She wrote clear instructions on a sticky note: water the ferns on Wednesday and keep the orchids near indirect sunlight. 
                I followed her note carefully, and when she returned, all the green leaves were flourishing.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Anne talimatları nereye yazmıştı?", "Yapışkanlı bir not kağıdına", listOf("Yapışkanlı bir not kağıdına", "Deftere", "Mesaj olarak", "Buzdolabı magnetine")),
                ReadingQuizQuestion("Eğrelti otları (ferns) hangi gün sulanmalıydı?", "Çarşamba günü", listOf("Çarşamba günü", "Pazartesi", "Cuma", "Her gün")),
                ReadingQuizQuestion("Orkidelerin nerede durması gerekiyordu?", "Dolaylı güneş ışığı alan yerde", listOf("Dolaylı güneş ışığı alan yerde", "Tam güneşin altında", "Karanlık odada", "Balkonda"))
            )
        ),
        ReadingStory(
            title = "Learning to Swim",
            level = "A2",
            content = """
                Last summer, I joined a beginner swimming course at the municipal sports center. 
                In the first lesson, I was afraid of putting my face into the deep water. 
                The coach was patient and taught us basic breathing techniques with a kickboard. 
                After four weeks of practice, I could swim across the pool without any help.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar yüzme kursuna ne zaman katıldı?", "Geçen yaz", listOf("Geçen yaz", "Geçen kış", "İki yıl önce", "Bu bahar")),
                ReadingQuizQuestion("Antrenör ilk derste neyle nefes tekniği öğretti?", "Yüzme tahtasıyla (kickboard)", listOf("Yüzme tahtasıyla (kickboard)", "Can simidiyle", "Kolluklarla", "Gözlükle")),
                ReadingQuizQuestion("Yazar kaç haftalık çalışmadan sonra tek başına yüzebildi?", "Dört hafta", listOf("Dört hafta", "İki hafta", "Altı hafta", "Bir hafta"))
            )
        ),
        ReadingStory(
            title = "At the Street Bazaar",
            level = "A2",
            content = """
                Every Thursday morning, a lively open-air market opens in our neighborhood. 
                Farmers bring fresh vegetables, fruits, and homemade cheeses from nearby villages. 
                My mother always bargains politely with the vendors to get reasonable prices. 
                Yesterday, we bought two kilos of sweet cherries and five fresh artichokes.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Semt pazarı hangi gün kurulur?", "Perşembe sabahı", listOf("Perşembe sabahı", "Salı sabahı", "Cumartesi", "Pazar")),
                ReadingQuizQuestion("Pazardan kaç kilo tatlı kiraz aldılar?", "İki kilo", listOf("İki kilo", "Bir kilo", "Üç kilo", "Beş kilo")),
                ReadingQuizQuestion("Anne satıcılarla ne yapar?", "Kibarca pazarlık yapar", listOf("Kibarca pazarlık yapar", "Tartışır", "Hemen öder", "Fiş ister"))
            )
        ),
        ReadingStory(
            title = "My Pet's Birthday",
            level = "A2",
            content = """
                Yesterday was my dog Toby's third birthday, so we planned a little celebration for him. 
                I baked homemade dog biscuits with oats, peanut butter, and bananas. 
                My sister bought him a yellow squeaky tennis ball. 
                When we gave him the toy, Toby ran around the living room wagging his tail excitedly.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Toby kaç yaşına bastı?", "Üç yaşına", listOf("Üç yaşına", "İki yaşına", "Dört yaşına", "Beş yaşına")),
                ReadingQuizQuestion("Ev yapımı köpek bisküvisinde ne vardı?", "Yulaf, fıstık ezmesi ve muz", listOf("Yulaf, fıstık ezmesi ve muz", "Et ve kemik", "Peynir ve un", "Elma ve havuç")),
                ReadingQuizQuestion("Kız kardeş Toby'ye hediye olarak ne aldı?", "Sarı ses çıkaran tenis topu", listOf("Sarı ses çıkaran tenis topu", "Yeni bir tasma", "Yatak", "Kıyafet"))
            )
        ),
        ReadingStory(
            title = "Rainy Day Board Games",
            level = "A2",
            content = """
                A heavy thunderstorm hit our city on Sunday, so we couldn't go to the park. 
                Instead, my cousins came to our house with two board games. 
                We sat around the carpet and played a word puzzle game for hours. 
                My mother prepared hot tea and buttered toast for us while rain tapped on the window glass.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Neden parka gidemediler?", "Şiddetli fırtına çıktığı için", listOf("Şiddetli fırtına çıktığı için", "Yoruldukları için", "Ders çalıştıkları için", "Park kapalı olduğu için")),
                ReadingQuizQuestion("Kuzenleriyle ne tür bir kutu oyunu oynadılar?", "Kelime bulmaca oyunu", listOf("Kelime bulmaca oyunu", "Satranç", "Strateji oyunu", "Kart oyunu")),
                ReadingQuizQuestion("Anne onlara ne ikram etti?", "Sıcak çay ve tereyağlı tost", listOf("Sıcak çay ve tereyağlı tost", "Kek ve meyve suyu", "Kurabiye ve kahve", "Çorba"))
            )
        ),
        ReadingStory(
            title = "The Local Art Exhibition",
            level = "A2",
            content = """
                Our town hall hosted an amateur art exhibition over the weekend. 
                Local artists displayed paintings of nature, portraits, and abstract ceramic sculptures. 
                One painting showed a foggy morning in our harbor with fishermen preparing their boats. 
                I liked it so much that I wrote a complimentary note in the guestbook.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Sergideki eserler kimler tarafından yapılmıştı?", "Yerel amatör sanatçılar", listOf("Yerel amatör sanatçılar", "Ünlü ressamlar", "Öğrenciler", "Tarihi ustalar")),
                ReadingQuizQuestion("Yazarın en çok beğendiği resim neyi anlatıyordu?", "Balıkçıların teknelerini hazırladığı sisli bir liman sabahını", listOf("Balıkçıların teknelerini hazırladığı sisli bir liman sabahını", "Bir dağ manzarasını", "Çiçek bahçesini", "Tarihi bir kaleyi")),
                ReadingQuizQuestion("Yazar sergiden ayrılmadan önce ne yaptı?", "Ziyaretçi defterine övgü dolu bir not yazdı", listOf("Ziyaretçi defterine övgü dolu bir not yazdı", "Resmi satın aldı", "Fotoğraf çekti", "Sanatçıyla röportaj yaptı"))
            )
        ),
        ReadingStory(
            title = "Spring Cleaning Day",
            level = "A2",
            content = """
                Every spring, our whole family dedicates a Saturday to deep clean the house. 
                My brother and I dusted the tall bookshelves and wiped the windows clean. 
                My father cleaned the garage and sorted out old tools. 
                Afterwards, we donated clothes we no longer wore to a local charity organization.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar ve kardeşi evin neresini temizledi?", "Kitaplıkların tozunu aldı ve camları sildi", listOf("Kitaplıkların tozunu aldı ve camları sildi", "Garajı temizledi", "Halıları yıkadı", "Bahçeyi düzenledi")),
                ReadingQuizQuestion("Artık giyilmeyen kıyafetlere ne yapıldı?", "Yardım kuruluşuna bağışlandı", listOf("Yardım kuruluşuna bağışlandı", "Çöpe atıldı", "Tavan arasına kaldırıldı", "Satıldı")),
                ReadingQuizQuestion("Temizlik hangi mevsimde yapıldı?", "İlkbahar", listOf("İlkbahar", "Sonbahar", "Yaz", "Kış"))
            )
        ),
        ReadingStory(
            title = "A Letter to a Pen Pal",
            level = "A2",
            content = """
                I have a pen pal named Lucas who lives in Lyon, France. 
                Last week, I received a handwritten letter from him describing his school life and French traditions. 
                I wrote back explaining Turkish festivals, especially how we share sweets during holidays. 
                Writing letters by hand feels much more personal than sending emails.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Mektup arkadaşı Lucas nerede yaşıyor?", "Lyon, Fransa'da", listOf("Lyon, Fransa'da", "Paris'te", "Londra'da", "Berlin'de")),
                ReadingQuizQuestion("Yazar mektubunda neyi anlattı?", "Türk bayramlarını ve tatlı paylaşma geleneğini", listOf("Türk bayramlarını ve tatlı paylaşma geleneğini", "Kendi okulunu", "Spor aktivitelerini", "Hava durumunu")),
                ReadingQuizQuestion("Yazar elle mektup yazmak hakkında ne düşünüyor?", "E-postadan çok daha samimi ve kişisel olduğunu", listOf("E-postadan çok daha samimi ve kişisel olduğunu", "Çok yorucu olduğunu", "Eski moda olduğunu", "Yavaş olduğunu"))
            )
        ),
        ReadingStory(
            title = "A Delayed Flight at the Airport",
            level = "A2",
            content = """
                When my uncle flew to visit us from Berlin, his flight was delayed by four hours due to dense fog. 
                We waited at the arrival terminal and drank hot coffee while watching planes land. 
                When my uncle finally walked through the exit gate, he was tired but smiled warmly and brought us German chocolate.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Uçuş neden dört saat ertelendi?", "Yoğun sis yüzünden", listOf("Yoğun sis yüzünden", "Fırtına yüzünden", "Teknik arıza yüzünden", "Kar yağışı yüzünden")),
                ReadingQuizQuestion("Amca hangi şehirden geliyordu?", "Berlin'den", listOf("Berlin'den", "Münih'ten", "Frankfurt'tan", "Viyana'dan")),
                ReadingQuizQuestion("Amca gelirken ne hediye getirdi?", "Alman çikolatası", listOf("Alman çikolatası", "Kitap", "Oyuncak", "Kahve"))
            )
        )
    )

    // ==========================================
    // ARŞİV HİKAYELERİ VE ANLAMA SORULARI (12 ADET)
    // ==========================================
    val archiveStories: List<ReadingStory> = listOf(
        ReadingStory(
            title = "My Special Day",
            level = "A1",
            content = """
                Today is a very special day for me. My name is Ali, and I want to tell you about my morning. 
                I usually wake up at seven o'clock, but today is different. Today is my birthday! 
                The sun is shining through my window. I feel very happy and energetic. 
                I get out of bed and go to the kitchen. I can smell fresh coffee and toasted bread. 
                My mother and father are there. They say, 'Happy Birthday, Ali!' and they give me a big hug. 
                Everything feels wonderful today.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Bugün Ali için neden özel bir gündür?", "Doğum günü olduğu için", listOf("Doğum günü olduğu için", "Okulun ilk günü olduğu için", "Tatile çıkacağı için", "Sınavı kazandığı için")),
                ReadingQuizQuestion("Ali mutfağa girdiğinde hangi kokuları alır?", "Taze kahve ve kızarmış ekmek", listOf("Taze kahve ve kızarmış ekmek", "Sıcak çay ve peynirli börek", "Pankek ve portakal suyu", "Sıcak süt ve kurabiye")),
                ReadingQuizQuestion("Ali'nin anne ve babası mutfakta ona ne yapar?", "Doğum gününü kutlayıp sıkıca sarılırlar", listOf("Doğum gününü kutlayıp sıkıca sarılırlar", "Ona doğum günü hediyesini verirler", "Kahvaltı hazırlamasına yardım ederler", "Birlikte doğum günü şarkısı söylerler"))
            )
        ),
        ReadingStory(
            title = "Daily Routine",
            level = "A1",
            content = """
                My name is Alex. I am a student. I wake up at 7 o’clock every day. 
                I wash my face and brush my teeth. Then I have breakfast. I drink tea and eat bread and cheese.
                After breakfast, I go to school. I listen to my teacher and take notes.
                At 12 o’clock, I have lunch. In the evening, I go home and have dinner with my family. 
                At night, I read a book. I go to bed at 11 o’clock.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Alex kahvaltıda ne tüketir?", "Çay içer, ekmek ve peynir yer", listOf("Çay içer, ekmek ve peynir yer", "Kahve içer, yumurta ve tost yer", "Portakal suyu içer ve krep yer", "Süt içer, bal ve tereyağı yer")),
                ReadingQuizQuestion("Alex okulda ders esnasında ne yapar?", "Öğretmenini dinler ve not alır", listOf("Öğretmenini dinler ve not alır", "Arkadaşlarıyla sohbet eder", "Sadece ders kitabını okur", "Ödevlerini tamamlamaya çalışır")),
                ReadingQuizQuestion("Alex gece uyumadan hemen önce ne yapar?", "Kitap okur", listOf("Kitap okur", "Müzik dinler", "Televizyon izler", "Ders tekrarı yapar"))
            )
        ),
        ReadingStory(
            title = "My Family",
            level = "A1",
            content = """
                I have a small family. There are four people in my family: my father, my mother, my sister, and me. 
                My father is a doctor. He is very kind. My mother is a teacher. She loves books. 
                My sister is five years old. She is very funny. We live in a big house with a garden. 
                I love my family very much.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın ailesinde kaç kişi yaşamaktadır?", "Dört kişi", listOf("Dört kişi", "Beş kişi", "Üç kişi", "Altı kişi")),
                ReadingQuizQuestion("Yazarın anne ve babasının meslekleri sırasıyla nelerdir?", "Baba doktor, anne öğretmendir", listOf("Baba doktor, anne öğretmendir", "Baba mühendis, anne doktordur", "Baba öğretmen, anne avukattır", "Baba eczacı, anne hemşiredir")),
                ReadingQuizQuestion("Yazarın ailesi nasıl bir evde oturmaktadır?", "Bahçeli büyük bir evde", listOf("Bahçeli büyük bir evde", "Şehir merkezinde küçük bir dairede", "Deniz kenarında iki katlı villada", "Geniş teraslı bir apartman dairesinde"))
            )
        ),
        ReadingStory(
            title = "My Dream House",
            level = "A1",
            content = """
                I want to live in a beautiful house. My dream house is near the sea. 
                It has two floors and four rooms. The living room is big and white. 
                The kitchen is modern. There is a balcony with a great view. 
                I want a small garden for my flowers. It is a very peaceful and happy place.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın hayalindeki ev nerede konumlanmıştır?", "Denizin yakınında", listOf("Denizin yakınında", "Dağ yamacında", "Şehir merkezinde", "Ormanın içinde")),
                ReadingQuizQuestion("Hayalindeki ev kaç katlı ve kaç odalıdır?", "İki katlı ve dört odalı", listOf("İki katlı ve dört odalı", "Üç katlı ve beş odalı", "Tek katlı ve üç odalı", "İki katlı ve altı odalı")),
                ReadingQuizQuestion("Yazar evin küçük bahçesini ne için istemektedir?", "Çiçekleri için", listOf("Çiçekleri için", "Sebze yetiştirmek için", "Köpeği için", "Açık hava kahvaltıları için"))
            )
        ),
        ReadingStory(
            title = "At the Grocery Store",
            level = "A1",
            content = """
                I am at the grocery store now. I need some food for dinner. 
                I want to buy milk, eggs, and bread. I also need some fresh fruit. 
                I like apples and bananas. I see the vegetables. I buy tomatoes and onions. 
                I go to the cashier and pay for my food. Now I can go home.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar marketten meyve olarak hangilerini satın alır?", "Elma ve muz", listOf("Elma ve muz", "Portakal ve çilek", "Üzüm ve şeftali", "Limon ve kivi")),
                ReadingQuizQuestion("Yazar sebze reyonundan ne alır?", "Domates ve soğan", listOf("Domates ve soğan", "Patates ve havuç", "Biber ve salatalık", "Marul ve ıspanak")),
                ReadingQuizQuestion("Yazar markete ne amaçla gitmiştir?", "Akşam yemeği için yiyecek almaya", listOf("Akşam yemeği için yiyecek almaya", "Sabah kahvaltılıklarını tamamlamaya", "Temizlik malzemeleri satın almaya", "Haftalık toplu alışveriş yapmaya"))
            )
        ),
        ReadingStory(
            title = "Summer Weather",
            level = "A1",
            content = """
                The weather is very hot today. It is summer. The sky is blue and the sun is bright. 
                I am wearing a t-shirt and shorts. My friends and I are at the beach. 
                We swim in the sea and play games. In the evening, it is warm. 
                I love summer because the days are long and sunny.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar ve arkadaşları sahilde ne yapmaktadır?", "Denizde yüzüp oyunlar oynarlar", listOf("Denizde yüzüp oyunlar oynarlar", "Güneşlenip kitap okurlar", "Sahil boyunca bisiklet sürerler", "Balık tutup kamp yaparlar")),
                ReadingQuizQuestion("Yazar sıcak havada ne giymiştir?", "Tişört ve şort", listOf("Tişört ve şort", "Gömlek ve keten pantolon", "Spor atlet ve eşofman", "Mayo ve şapka")),
                ReadingQuizQuestion("Yazar yaz mevsimini en çok neden sevmektedir?", "Günler uzun ve güneşli olduğu için", listOf("Günler uzun ve güneşli olduğu için", "Okullar tatil olduğu için", "Deniz suyu çok sıcak olduğu için", "Geceleri serin rüzgar estiği için"))
            )
        ),
        ReadingStory(
            title = "What Time Is It?",
            level = "A1",
            content = """
                Time is very important. I have a busy day today. 
                At 8 o'clock, I have a meeting. At 10 o'clock, I visit my grandmother. 
                We drink tea together. At 1 o'clock, I meet my friend for lunch. 
                In the afternoon, I go to the gym. I always check my watch because I don't want to be late.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar saat 10:00'da ne yapmaktadır?", "Büyükannesini ziyaret edip çay içer", listOf("Büyükannesini ziyaret edip çay içer", "İş toplantısına katılır", "Arkadaşıyla öğle yemeği yer", "Spor salonunda egzersiz yapar")),
                ReadingQuizQuestion("Arkadaşıyla öğle yemeği randevusu saat kaçtadır?", "Saat 13:00'te (1 o'clock)", listOf("Saat 13:00'te (1 o'clock)", "Saat 12:00'de (12 o'clock)", "Saat 14:00'te (2 o'clock)", "Saat 11:30'da (half past 11)")),
                ReadingQuizQuestion("Yazar neden sürekli saatini kontrol eder?", "Programına geç kalmak istemediği için", listOf("Programına geç kalmak istemediği için", "Saatinin çalışıp çalışmadığını merak ettiği için", "Toplantının ne zaman biteceğini görmek için", "Otobüsü kaçırmaktan korktuğu için"))
            )
        ),
        ReadingStory(
            title = "My Favorite Hobbies",
            level = "A1",
            content = """
                I have many hobbies. I like playing the guitar and listening to music. 
                On weekends, I go to the park and ride my bike. 
                I also love taking photos of nature. Cooking is another hobby for me. 
                I like making pizza for my friends. Hobbies make my life very interesting and fun.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar hafta sonları parkta hangi aktiviteyi yapar?", "Bisiklet sürer", listOf("Bisiklet sürer", "Koşu yapar", "Gitar çalar", "Doğa fotoğrafları çeker")),
                ReadingQuizQuestion("Yazar arkadaşları için yemek olarak ne pişirmeyi sever?", "Pizza", listOf("Pizza", "Makarna", "Hamburger", "Kek")),
                ReadingQuizQuestion("Metne göre yazarın müzikle ilgili hobisi nedir?", "Gitar çalmak ve müzik dinlemek", listOf("Gitar çalmak ve müzik dinlemek", "Piyano çalmak ve şarkı söylemek", "Konserlere gitmek ve kaset toplamak", "Keman dersi almak"))
            )
        ),
        ReadingStory(
            title = "Where Is The Bank?",
            level = "A1",
            content = """
                Excuse me, I am lost. I am looking for the bank. 
                Is it near here? Go straight and turn left at the pharmacy. 
                The bank is next to the library. It is a big grey building. 
                Thank you very much for your help! You are welcome. Have a nice day.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Bankayı tarif eden kişi ilk olarak hangi yönergeleri verir?", "Düz git ve eczaneden sola dön", listOf("Düz git ve eczaneden sola dön", "Sağa dön ve fırının önünden geç", "Düz git ve kütüphaneden sağa dön", "Işıklardan sola dön ve parkı geç")),
                ReadingQuizQuestion("Banka binası hangi yapının hemen yanındadır?", "Kütüphanenin yanında", listOf("Kütüphanenin yanında", "Eczanenin yanında", "Postanenin yanında", "Okulun yanında")),
                ReadingQuizQuestion("Banka binasının görünüşü nasıldır?", "Büyük gri bir bina", listOf("Büyük gri bir bina", "Eski kırmızı tuğlalı bir bina", "Cam kaplı yüksek bir gökdelen", "Mavi boyalı iki katlı bir bina"))
            )
        ),
        ReadingStory(
            title = "Feeling Sick",
            level = "A1",
            content = """
                Today, I don't feel well. I have a headache and a cold. 
                I think I have a fever. I stay in bed and rest. 
                I drink a lot of water and orange juice. My mother makes chicken soup for me. 
                I need to see a doctor tomorrow. I hope I feel better soon.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazarın bugünkü sağlık şikayetleri nelerdir?", "Baş ağrısı, soğuk algınlığı ve ateş", listOf("Baş ağrısı, soğuk algınlığı ve ateş", "Boğaz ağrısı ve mide bulantısı", "Diş ağrısı ve yüksek tansiyon", "Öksürük ve kas krampları")),
                ReadingQuizQuestion("Yazarın annesi onun için ne hazırlar?", "Tavuk çorbası", listOf("Tavuk çorbası", "Bitki çayı ve bal", "Sebze çorbası", "Sıcak süt")),
                ReadingQuizQuestion("Yazar iyileşmek için gün içinde neler tüketir?", "Bol su ve portakal suyu", listOf("Bol su ve portakal suyu", "Sıcak kahve ve limonlu çay", "Sadece maden suyu", "İlaç ve enerji içeceği"))
            )
        ),
        ReadingStory(
            title = "Weekend In Paris",
            level = "A1",
            content = """
                I am very excited. I am in Paris for the weekend. 
                I want to see the Eiffel Tower and visit museums. 
                The city is very beautiful and old. I eat delicious croissants for breakfast. 
                I walk on the streets and take many pictures. Paris is a wonderful city for a trip.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar Paris'te kahvaltıda ne yer?", "Lezzetli kruvasanlar", listOf("Lezzetli kruvasanlar", "Fransız omleti ve peynir", "Tost ve sıcak çikolata", "Pankek ve taze meyve")),
                ReadingQuizQuestion("Yazar şehirde nereleri görmeyi planlamaktadır?", "Eyfel Kulesi'ni ve müzeleri", listOf("Eyfel Kulesi'ni ve müzeleri", "Tarihi sarayları ve botanik parkını", "Seine Nehri'ni ve opera binasını", "Katedralleri ve sanat akademisini")),
                ReadingQuizQuestion("Yazar sokaklarda gezerken ne yapar?", "Yürüyüş yapıp birçok fotoğraf çeker", listOf("Yürüyüş yapıp birçok fotoğraf çeker", "Hediyelik eşya dükkanlarını dolaşır", "Yerel insanlarla röportaj yapar", "Resim çizer ve not alır"))
            )
        ),
        ReadingStory(
            title = "Future Plans",
            level = "A1",
            content = """
                I have big plans for the future. Next year, I want to learn a new language. 
                I also want to travel to Spain and Italy. I plan to work in a big company. 
                I want to buy a new car and help my parents. I study hard every day to achieve my dreams.
            """.trimIndent(),
            questions = listOf(
                ReadingQuizQuestion("Yazar gelecek yıl hangi ülkelere seyahat etmek istemektedir?", "İspanya ve İtalya", listOf("İspanya ve İtalya", "Fransa ve Almanya", "İngiltere ve Hollanda", "Portekiz ve Yunanistan")),
                ReadingQuizQuestion("Yazar kariyeriyle ilgili ne planlamaktadır?", "Büyük bir şirkette çalışmayı", listOf("Büyük bir şirkette çalışmayı", "Kendi işletmesini kurmayı", "Yurt dışında akademisyen olmayı", "Devlet dairesinde memur olmayı")),
                ReadingQuizQuestion("Yazar hayallerine ulaşmak için şu an ne yapmaktadır?", "Her gün çok sıkı ders çalışır", listOf("Her gün çok sıkı ders çalışır", "Yarı zamanlı bir işte para biriktirir", "Sürekli seyahat rehberleri okur", "Dil kurslarına başvurur"))
            )
        )
    )

    // Tüm hikayeleri başlık -> içerik şeklinde sunan ana harita
    val allStoriesMap: Map<String, String> = (a1Stories + a2Stories + archiveStories).associate { it.title to it.content }

    // Her hikaye başlığına özel hazırlanan 3'er adet anlama sorusu haritası
    val allQuestionsMap: Map<String, List<ReadingQuizQuestion>> = (a1Stories + a2Stories + archiveStories).associate { it.title to it.questions }

    fun getQuestionsFor(title: String): List<ReadingQuizQuestion>? = allQuestionsMap[title]
}
