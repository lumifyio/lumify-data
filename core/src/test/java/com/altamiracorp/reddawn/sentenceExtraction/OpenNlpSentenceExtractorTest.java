package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

@RunWith(JUnit4.class)
public class OpenNlpSentenceExtractorTest {
    private OpenNlpSentenceExtractor extractor;
    private String sentenceModelFile = "en-sent.bin";
    private Date createDate = new Date();

    @Before
    public void setUp() throws IOException {
        extractor = new OpenNlpSentenceExtractor() {
            @Override
            public void setup(Mapper<Text, Artifact, Text, Sentence>.Context context) throws IOException {
                InputStream sentenceModelIn = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(sentenceModelFile);
                SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
                setSentenceDetector(new SentenceDetectorME(sentenceModel));
                setMaxLength(1000);
            }

            @Override
            protected Date getDate() {
                return createDate;
            }
        };

        extractor.setup(null);
    }

    @Test
    public void testExtractionOfTwoSentences() {
        Artifact artifact = new Artifact("urn:sha256:abcd");
        artifact.getGenericMetadata().setAuthor("author").setSubject("Sample Subject");
        artifact.getGenericMetadata().setMimeType("document");
        artifact.getContent().setSecurity("U");
        String text = "This is some text. It has two sentences.";
        artifact.getContent().setDocExtractedText(text.getBytes());

        Collection<Sentence> sentences = extractor.extractSentences(artifact);
        assertEquals(2, sentences.size());

        Iterator<Sentence> iterator = sentences.iterator();

        Sentence sentence = iterator.next();
        assertEquals("urn:sha256:abcd:0000000000000018:0000000000000000", sentence.getRowKey().toString());
        assertEquals("urn:sha256:abcd", sentence.getData().getArtifactId());
        assertEquals(Long.valueOf(0), sentence.getData().getStart());
        assertEquals(Long.valueOf(18), sentence.getData().getEnd());
        assertEquals("This is some text.", sentence.getData().getText());
        assertEquals("author", sentence.getMetadata().getAuthor());
        assertEquals((Long) createDate.getTime(), sentence.getMetadata().getDate());
        assertEquals("OpenNLP", sentence.getMetadata().getExtractorId());
        assertEquals("U", sentence.getMetadata().getSecurityMarking());
        byte[] md5 = new byte[] { 90, 66, -31, -14, 119, -5, -58, 100, 103, 124, 45, 41, 7, 66, 23, 107 };
        assertArrayEquals(md5, sentence.getMetadata().getContentHash());

        sentence = iterator.next();
        assertEquals("urn:sha256:abcd:0000000000000040:0000000000000019", sentence.getRowKey().toString());
        assertEquals("urn:sha256:abcd", sentence.getData().getArtifactId());
        assertEquals(Long.valueOf(19), sentence.getData().getStart());
        assertEquals(Long.valueOf(40), sentence.getData().getEnd());
        assertEquals("It has two sentences.", sentence.getData().getText());
        assertEquals("author", sentence.getMetadata().getAuthor());
        assertEquals((Long)createDate.getTime(), sentence.getMetadata().getDate());
        assertEquals("OpenNLP", sentence.getMetadata().getExtractorId());
        assertEquals("U", sentence.getMetadata().getSecurityMarking());
        md5 = new byte[] { 12, 80, -119, -97, 22, -3, 53, -14, 86, -44, -28, -53, 111, -32, -46, 103 };
        assertArrayEquals(md5, sentence.getMetadata().getContentHash());
    }

    @Test
    public void testExtractionOfTwoSentences_NoAuthorNoSecurity() {
        Artifact artifact = new Artifact("urn:sha256:abcd");
        String text = "This is some text. It has two sentences.";
        artifact.getContent().setDocExtractedText(text.getBytes());
        artifact.getGenericMetadata().setMimeType("document");
        artifact.getGenericMetadata().setSubject("sample subject");

        Collection<Sentence> sentences = extractor.extractSentences(artifact);
        assertEquals(2, sentences.size());

        Iterator<Sentence> iterator = sentences.iterator();

        Sentence sentence = iterator.next();
        assertNotSame("author", sentence.getMetadata().getAuthor());
        assertNotSame("U", sentence.getMetadata().getSecurityMarking());

        sentence = iterator.next();
        assertNotSame("author", sentence.getMetadata().getAuthor());
        assertNotSame("U", sentence.getMetadata().getSecurityMarking());
    }

    @Test
    public void testExtractionOfTwoSentences_LongText() {
        Artifact artifact = new Artifact("urn:sha256:abcd");
        artifact.getGenericMetadata().setAuthor("author").setSubject("Sample Subject");
        artifact.getGenericMetadata().setMimeType("document");
        artifact.getContent().setSecurity("U");
        String text = "Iraq's Baby Noor: An unfinished miracle\n" +
                "American soldiers plucked the child from her Iraqi home at the height of the war and brought her to America for life-saving surgery. But how did she fare after her return to a war-torn nation struggling to stand on its own?\n" +
                "Story by Moni Basu • Video by David S. Holloway and Brandon Ancil\n" +
                "Baghdad (CNN) -- The frail little girl wears a blue and gray striped dress with matching gladiator sandals and bag. Her grandfather pulls her aluminum wheelchair out of a dust-blanketed taxi, places her in the seat. Her legs dangle like a broken doll's.\n" +
                "Dr. Saad Nasser welcomes the girl as she is wheeled into his office, a room not much larger than a walk-in closet. A fan strains to ward off the smell of fresh paint. It's hard to tell this is a physician's office except for the posters advertising medications like Panadol and Novalac. There's little in the way of equipment.\n" +
                "Nasser leans over the girl on his patient bed.\n" +
                "\"Where does it hurt?\" he asks.\n" +
                "\"In my head,\" the girl answers, lifting her hand to her forehead. She tells him the pain is constant.\n" +
                "The doctor has been examining the girl for about five years. She is 7 now.\n" +
                "He tries to voice optimism, tells her she is doing well. He says she should be seeing a neurologist and other specialists, not a family physician like him. She should have a CT scan done every three months, but her family cannot afford it. She has a shunt in her brain that could need replacement. She suffers from urinary tract infections that result from abnormal bladder function. He fears further complications.\n" +
                "He knows there is little chance of a normal life for her here in war-ravaged Iraq.\n" +
                "The girl, like her homeland, is struggling. Hope for the future is fading.\n" +
                "She is strong, the doctor says. But it is very hard for her in Iraq. He says she may even die.\n" +
                "A broken girl in a broken land\n" +
                "Royal Jordanian Flight 8613 begins its descent into Baghdad on this late February night. It has been five years since I was last in Iraq. I strain to see out the window; I know I am nearing the city when blackness over Anbar province gives way to the twinkle of low-voltage lighting.\n" +
                "It is a strange feeling returning to this place where I spent so many months of my life. Covering the war, I had found a connection here to a people I did not know before.\n" +
                "I am making this long journey now in search of a little girl.\n" +
                "I'd always thought of her as a metaphor for the war. She was someone whom the Americans saved, just like they saved Iraq from Saddam Hussein. But now she was unfinished business. Forgotten by America.\n" +
                "A decade after the U.S. invasion of Iraq, she is a broken girl in a broken land.\n" +
                "Things did not go as expected after U.S. troops toppled Hussein. In years of lethal occupation, America found itself running a nation about which it knew little. American men and women were dying every month, as were thousands of Iraqis. Many soldiers I met struggled to make sense of a perplexing mission. They found meaning in small acts of humanity.\n" +
                "Noor al-Zahra Haider was born with spina bifida, a birth defect in which the vertebrae do not form completely around the spinal cord. Doctors in Iraq said she was certain to die.\n" +
                "Curtis Compton/\n" +
                "The girl, Noor al-Zahra Haider, was the beneficiary of one of those acts.\n" +
                "She was not even 3 months old yet when I first saw her, suffering from a severe spinal cord birth defect that was certain to kill her. She was discovered by soldiers patrolling impoverished Abu Ghraib -- the town notorious for its high-security prison -- and shuttled to America for life-saving surgery.\n" +
                "She became known to the world as Baby Noor.\n" +
                "Her smile enthralled everyone who saw her on television screens and newspaper pages. She was labeled Iraq's miracle baby.\n" +
                "Her name means \"light\" in Arabic -- she was a flicker of brightness in the midst of war's gloom.\n" +
                "Jeff Morgan, then an Army National Guard lieutenant, spearheaded the effort to fly Noor out of Iraq. The soldiers, he told me, felt compelled to do the right thing.\n" +
                "But there was little if any discussion about what would become of Noor after her stay in the United States.\n" +
                "What would happen to a child with a complicated medical condition who might need more surgery in the future? A child without regular bladder function? A girl who could not walk?\n" +
                "In a nation of bloodshed and pervasive fear, even the noblest of deeds cannot be assured a happy ending.\n" +
                "After six months of treatment in a children's hospital in Atlanta and care in the homes of two suburban families, Noor returned to Iraq.\n" +
                "There, her family would pay a price for accepting American help.\n" +
                "I caught up with Noor and her family in 2007, almost a year after her return. I went with them to see a doctor in what was then known as Baghdad's Green Zone. I last saw them in February 2008. Noor was 2½ then. She slithered along the grass like a snake, unable to stand or walk.\n" +
                "I wondered what would happen as she grew older in a harsh place like Iraq where, even before the war, care for children with disabilities was nominal at best. After years of punishing international sanctions under Hussein's rule and then war, children like Noor were an afterthought.\n" +
                "Five more years have passed. It was difficult to retain contact with them. No one in the family spoke English. Postal service was limited. Their telephone numbers changed as did their address. Everyone in America who was involved in Noor's care -- the soldiers, host families, doctors and the charity that shouldered the costs -- lost touch with her.\n" +
                "Now, as the Airbus jet sets down on Iraqi soil, I grow impatient to see her. Who is taking care of her? Does she still have that smile that melted hearts? I cannot wait for the sun to rise.\n" +
                "'Do you remember me?'\n" +
                "Our SUV makes its way through the chaos of Baghdad. Demonstrations by disenfranchised Sunnis earlier in the week closed off all the roads in and out of the central city. This morning, we are lucky, though we get news of bombings in two neighborhoods and in Mahmoudiya, about 25 miles south of here.\n" +
                "Security is tight in Baghdad, where sectarian tensions still run high. It's impossible to go anywhere without encountering concrete blast walls and checkpoints where Iraqi police search under cars for sticky bombs and use a hand-held device with an antenna to detect explosives.\n" +
                "Some people fear that outright civil war is a certainty in this splintered land.\n" +
                "Baghdad is a city trying to recover, but many neighborhoods look weary from years of conflict. In Noor's neighborhood, construction on the streets started but never finished.\n" +
                "David S. Holloway/CNN\n" +
                "We make our way south to Al Alam, now a largely Shiite neighborhood in southwest Baghdad where flags bearing images of Shiite saint Imam Hussein flutter in the wind.  We turn off a busy road and enter a neighborhood of concrete homes painted in peanut butter hues of dust and desert.\n" +
                "The streets are under construction -- or were. Some company secured a government contract to fix the potholes and began digging up the roads but never finished. Above, hundreds of thin electric lines crisscross in a spaghetti-like jumble, connecting homes to private generators so people can have light and the comfort of a fan when the power goes out.\n" +
                "The only bursts of color here are the fire-engine red plastic tanks that collect and supply water to homes and the orange sun protectors that shade patios. And the posters for Fanta and Pepsi at the corner store.\n" +
                "I stare out the car window. Iraq, I think, will never recover in my lifetime.\n" +
                "The driver follows the directions Noor's grandfather, Khalaf Abbas, has provided and I finally arrive at a sheet-metal gate. Before I can knock, the gate opens. The family has been eagerly awaiting my arrival.\n" +
                "\"We are so happy to see you,\" says Noor's father, Haider, through a translator.\n" +
                "Noor, I am told, was waiting by the gate for much of the morning and only just went inside. I realize I am late.\n" +
                "I walk into a room full of people: Haider's sisters, Zainab and Hijran, who still live at home; his second wife, Fatima; their two young sons; and Zainab's fianc?, Qaddory Sultan.\n" +
                "Everyone is dressed in their finest. Haider and his father are wearing suits that put them oddly out of place in this modest home. But I am not noticing much at this moment.\n" +
                "My eyes fall on Noor.\n" +
                "She is no longer a cute, chubby baby. She has grown into a skinny 7-year-old. Sadness blankets her face; on this day, she rarely smiles.\n" +
                "I give her a big hug and a kiss. I tell her she looks beautiful in her embellished cream and maroon dress. Zainab tells me Noor insists on dressing immaculately. Her thick black hair is always decorated with colorful clips and ties. She wears matching shoes and a necklace I am sure she has borrowed from an aunt.\n" +
                "\"Do you remember me?\" I ask.\n" +
                "She cannot possibly. She was so young when I saw her last. But she thinks she does. She has been shown so many photographs of her odyssey, told so many stories about how the Americans saved her. She has been told who I am; that I have come from America, from the city she once visited.\n" +
                "The family saved all the English-language books that were given to Noor in Atlanta. \"Cluck, Cluck, Who's There?\" and \"Goodnight Moon.\" Zainab keeps them stored for safekeeping until Noor learns to read English.\n" +
                "Zainab shows me an album brimming with baby photos of Noor and a stack of old newspaper stories written by me. It is another reminder of how much time has gone by. And how everything in Iraq ages so much faster. The pages are yellowed and tattered.\n" +
                "Noor flips through the album. I talk with the family about everything that happened, starting with that chilly December night when a chance encounter with American soldiers forever altered the course of their lives.\n" +
                "A raid and a rescue\n" +
                "The soldiers of Charlie Company burst into Khalaf Abbas' family home in a routine raid and search mission. It was the end of 2005, and American soldiers operated with a guiding principle: suspect everything and everyone.\n" +
                "Trust was not a part of the vocabulary in Iraq; it still isn't.\n" +
                "Noor, 7, eats her lunch on the floor of her Baghdad home. She's a light eater because of her bladder problems that are typical of children with spina bifida.\n" +
                "David S. Holloway/CNN\n" +
                "How many men are in the family? Do you know anyone involved in insurgent activity? Are you aware of criminals in your neighborhood?\n" +
                "Khalaf's wife, Soad, answered the soldiers' questions through an Army translator.\n" +
                "She was the matriarch of the family, the one who possessed enough strength to carry her entire tribe. She'd had brushes with the Americans before.\n" +
                "A neighbor was hit by an American bullet intended for insurgents. An American tank rolled over a kiosk that she and others used at the local market. Her eldest son, Bashar, was detained on suspicion of firing a rocket-propelled grenade, and then again just days before Charlie Company pounded fists on her door.\n" +
                "Soad knew this would be her only chance to speak to the Americans. She asked the Georgia Army National Guard soldiers to help her find Bashar. What were the charges against him, she asked.\n" +
                "As the soldiers turned to go, Soad made an even bolder move. She took Sgt. Nicholas Jelks over to her granddaughter Noor in the dimly lit family room and turned her on her belly, revealing a large, purplish tumor on Noor's back.\n" +
                "Soad had taken Noor to see doctors in Abu Ghraib. But there was nothing they could do for the baby.\n" +
                "\"She has at most 40 days,\" the doctors told her. \"Take her home to die.\"\n" +
                "Soad turned to Jelks. \"If you want to help Iraq,\" she blurted out, \"you will help her instead of bothering the innocent.\"\n" +
                "The platoon's teenage medic, Pfc. Justin Donnelly, carried a digital camera on every patrol. He began taking pictures of Noor and with a few clicks of a camera, counterinsurgency melted into compassion.\n" +
                "The photos went back to Camp Liberty, where Jeff Morgan, the platoon lieutenant, showed them to Army doctors. He was determined to find a way to save Noor's life.\n" +
                "I was an Atlanta Journal-Constitution reporter embedded with Charlie Company back then and lived in a trailer at Camp Liberty. I learned from the soldiers that Noor was born in September 2005, with spina bifida, a birth defect in which the vertebrae do not form completely around the spinal cord.\n" +
                "She had the double misfortune, in Iraqi society, of being born a girl with a severe defect; many saw her as a liability. They called her lame, a reject.\n" +
                "Even Noor's mother, Iman, rejected her newborn. She had wanted a boy. Instead, she had a girl with a disability. She refused to breast-feed her. The other women of the family stepped in.\n" +
                "\"Apparently, she didn't have any connection with her child,\" says Noor's aunt Hanan about Iman. \"She neglected Noor from the very first day.\"\n" +
                "For the soldiers, saving this child offered a chance at tangible victory plucked from the chaos of combat. They were eager to deliver good news from Iraq at a time when support for the war was waning at home.\n" +
                "Plus, it was a few days before Christmas.\n" +
                "Morgan told me he could not fathom being unable to access medical care that could save a child's life. He had five children of his own.\n" +
                "News of Baby Noor traveled fast -- all the way up the chain of command of the 10th Mountain Division brigade to which Charlie Company was attached. Within days, I was in the back of a Humvee, heading to Noor's house in Abu Ghraib on a military mission to fetch the baby.\n" +
                "In the dead of night, we walked on unpaved roads through mud and sewage the color of fluorescent green antifreeze. The soldiers kept an eye out for rooftop snipers.\n" +
                "In the house, we found Soad and Haider ready to go -- against the advice of neighbors who warned they would be threatened, against the wishes even of some family members who worried for their safety. Others voiced concern the family would lack the means to provide the lifetime of care Noor would need after complicated surgery.\n" +
                "But for Soad, the path was clear. It was a matter of life and death for Noor. Their tumultuous journey had begun.\n" +
                "Iraq's miracle baby\n" +
                "The military worked furiously to figure out the logistics of ferrying Iraqi civilians out of the country on U.S. transport. But it took awhile.  I spent my days with Soad, Haider and Noor at Camp Liberty. They stood out on the base full of infantrymen.\n" +
                "We all took turns helping Soad care for Noor. One sergeant helped feed her. Other soldiers took their Bradley Fighting Vehicles on a mission to buy diapers and baby formula.\n" +
                "Noor's grandfather and aunt push her wheelchair from their Baghdad home to the main road, where a minivan picks her up for school.\n" +
                "David S. Holloway/CNN\n" +
                "But even amid the generosity, suspicion lingered.\n" +
                "A soldier guarding Noor's door leapt from his chair one day when Haider stepped out to use his cell phone. Makeshift bombs, the top killer of U.S. troops in Iraq, were often detonated with mobile phones.\n" +
                "Soon news came that an Atlanta-based Christian nonprofit group called Childspring International would help. The charity brings sick children from the developing world to the United States for medical care.\n" +
                "On New Year's Eve, I watched the family board a C-130 at the Baghdad airport. No one there that day could have ever imagined an infant aboard a military plane. I stood there squinting upward at a cloudless Baghdad sky until the plane disappeared from view.\n" +
                "At the other end of the journey, hordes of media awaited the arrival of Baby Noor at the Atlanta airport. She was whisked away to the home of a host family, and within days doctors at Children's Healthcare of Atlanta examined Noor.\n" +
                "The state-of-the-art hospital is reputed for treating cancer, blood disorders and orthopedic problems. Soad and Haider were in awe. Their idea of medical care was limited to an Abu Ghraib clinic with dirty terrazzo floors, shattered windows and a few shelves holding medications obtained on the black market or from the U.S. military.\n" +
                "In January 2006, doctors began performing the first of several operations. Noor underwent surgery to realign and enclose her spinal column and then orthopedic surgery to release congenitally shortened tendons and overly tight ligaments in the back of her left ankle.\n" +
                "Dr. Roger Hudgins, a pediatric neurosurgeon, inserted a shunt or tube to drain the fluid that collected beneath the outer membrane covering the brain.\n" +
                "Hudgins was pleased with how well Noor responded, although he knew she would need a lifetime of medical monitoring. He also knew she would experience bladder problems common with spina bifida babies.\n" +
                "Soad told Hudgins that Noor was strong from her very first days. She asked if her granddaughter would walk, if she would lead a normal life.\n" +
                "Hudgins paused. Already, he had told the family that Noor would always need a wheelchair. That no matter what he did, she would never gain use of her legs. But how could he dash their yearning for a miracle?\n" +
                "He told them: \"I'm not here to take away hope. Time will tell.\"\n" +
                "No regrets\n" +
                "Soad and Haider moved into the suburban home of Nancy and Edward Turner as Noor recuperated from her surgeries.\n" +
                "Mother and son marveled at life in America.\n" +
                "Haider developed a taste for Cheetos and Kentucky Fried Chicken; he and Soad stood in awe of supermarket spigots that sprayed a fine mist over produce to keep them fresh. In Abu Ghraib, flies swarmed the market, landing on everything from tubs of pickled vegetables to freshly slaughtered sheep.\n" +
                "Haider and Soad admired things Americans take for granted: a central vacuum system, clean roads and a lush landscape of tall pines and oaks.\n" +
                "Emboldened in America, Soad uncovered her head and asked Nancy to help dye her hair a Revlon red. Haider took to making syrupy sweet Iraqi-style tea for the Turners.\n" +
                "But life began unraveling when the phone calls started.\n" +
                "Haider received threats from people he believes were linked to al Qaeda in Iraq. They accused him of spying for the Americans.\n" +
                "Back home, the family's bakery was bombed. If Haider and Soad did not return to Iraq, the callers said, the whole family would pay.\n" +
                "They were left with little choice.\n" +
                "\"I couldn't take Noor back without finishing her care,\" Soad told me later from Baghdad. \"I couldn't lose my family either.\"\n" +
                "Jeff Morgan, then an Army lieutenant, was determined to help Noor after soldiers from his company discovered her in December 2005. Noor's grandmother Soad, left, knew the Americans were Noor's only hope for survival.\n" +
                "Curtis Compton/\n" +
                "Atlanta Journal-Constitution via AP\n" +
                "They returned to Iraq, leaving Noor, still under medical supervision of U.S. doctors, with Nancy, who cared for her like a mother for another three months. In June, when Noor was ready, Nancy flew back with her to Kuwait. From there, the U.S. military returned her home to Abu Ghraib.\n" +
                "No one doubted that saving Noor's life was a good thing. But the threats to the family cast the decision in a new light: Should she have been brought to America? Could anyone have foreseen the repercussions to the family for the association with Americans?\n" +
                "A few days before Noor returned to Iraq, I went to see her. Morgan, the Army lieutenant, had recently returned from his combat tour. It was Father's Day, and he'd brought his own children to meet Noor.\n" +
                "I asked if he had any regrets about his role in bringing the Iraqi baby to America.  He had none, but he also said this: \"Maybe it was right. Maybe it was wrong.\"\n" +
                "The price for American help\n" +
                "At a roadside stall in their Baghdad neighborhood of Al Alam, Haider and his brother Bashar sell fresh fruits and vegetables. They are surrounded by a field of garbage where young boys playing soccer must dodge used hypodermic needles, paint cans and plastic bags.\n" +
                "Next to Haider's stall, a herder waits to slaughter the first sheep of the day. Nine sheep are tied up, oblivious that they'll end up on a dinner plate.\n" +
                "Haider and Bashar buy their produce from a wholesaler and make just enough from retail customers to get by. About 20% rot and go to waste; they dump it in the field for the sheep to finish off.\n" +
                "Haider unloads the truck and swats away clouds of flies.\n" +
                "\"It is very different from Atlanta,\" he tells me, remembering his trips with Nancy to the supermarket. He knows now what it means to be comfortable in life; it was that much harder to move forward once he returned from America.\n" +
                "He recounts for me the troubles he and his family faced when they returned to Abu Ghraib. I learn details that before were sketchy.\n" +
                "Bashar was detained again in July 2006 by U.S. forces and held for five years. The family says there were never any charges leveled against him. Haider shows me Department of Defense documents issued to the family. They confirm Bashar spent time at Camp Bucca, a U.S. detention facility in southern Iraq.\n" +
                "Ten days after Haider and Soad came back home from Atlanta, Haider tells me, he was abducted by three gunmen in a truck. They blindfolded him, tied his hands behind his back, drove him around Baghdad and then shoved him out.\n" +
                "\"Let's kill him now,\" Haider heard one of them say. \"Tell us. Are you spying for the Americans?\n" +
                "That's when Haider told them about his daughter and how he went to America only for her sake.\n" +
                "Haider believes the gunmen checked out his story. They spared his life but decided to hold him for $20,000 in ransom.\n" +
                "His sisters sold the gold jewelry they received for their weddings. They borrowed money from relatives. Somehow they eked out enough to make the payment.\n" +
                "A few months later, they sold their home in Abu Ghraib and moved to a smaller, more rustic rented house in Al Alam, where no one knew their story.\n" +
                "Noor's mother, Iman, left the family with her second child, Karar, and asked for a divorce. Noor is growing up without knowing her brother and rarely sees her mother anymore.\n" +
                "Noor's grandmother adopted Noor as her own child, but the family was anxious for Haider to remarry and offered a proposal to Fatima, a family friend.\n" +
                "Fatima says her brothers warned her about marrying a man with a disabled child. One day, they said, the grandparents will die, the sisters who were still single will marry and leave home and the child will fall on you.\n" +
                "Khalaf Abbas worries about the future of his granddaughter. Who will take care of Noor as she grows into womanhood? He says his family needs help.\n" +
                "David S. Holloway/CNN\n" +
                "Fatima told them: \"I will take care of Noor. Not because of Haider but because she is a child in need of love.\"\n" +
                "A special school\n" +
                "On a Sunday morning, the start of the workweek in Iraq, Fatima and Haider's sister Zainab get Noor ready for school. The family tried to enroll her in a regular elementary school. But none would take her.\n" +
                "So she attends one of two government-run institutions for disabled children in Baghdad. It's far from Noor's house, but there was no other choice. Sometimes when the government-run van doesn't show up, Noor has to miss school. Her grandfather says he cannot afford to pay the $8 for a taxi ride there and back.\n" +
                "Fatima lays Noor down on the couch to straighten her tights. She's wearing a black jumper with a ruffled white blouse that could rival any Elizabethan collar.\n" +
                "\"I always choose the best and most beautiful outfits,\" Zainab tells me. \"I don't want (Noor) to feel less than others or to think that anyone is more beautiful or better dressed than her. I don't want her to have that in her heart.\"\n" +
                "After she is ready, Noor's grandfather wheels her down to the main road. They make a regular stop at the convenience store so Noor can buy milk and snacks for the school day. Often that includes chocolate.\n" +
                "A maroon minivan arrives to pick her up. Already on board are five children; the driver snakes his way around several neighborhoods to pick up three more.\n" +
                "Some of the students are mentally disabled. Others, physically. Some are both. One girl, Jenat, used to attend a regular elementary school until a gland problem led to obesity and she was bullied. The principal told Jenat's mother to enroll her in Noor's school.\n" +
                "The children sing songs, tell jokes. The teacher who rides with them tries to keep them engaged. Noor sits in a window seat, quiet the entire way.\n" +
                "During the ride, the kids talk about pacha, a traditional Iraqi dish made from the sheep's head. Everyone lets out expressions of disgust. Not Noor.\n" +
                "\"Noor never says anything unless you ask her a question,\" says Maryam, one of the kids in the van.\n" +
                "When the van finally arrives at the school, teachers roll out wheelchairs for the kids. Noor's first class this morning is Arabic. The teacher asks her to come to the white board to write a sentence.\n" +
                "\"I am helping my mother,\" she writes. My Arabic skills are hardly good enough to judge Noor's script, but I notice she is left-handed.\n" +
                "I step outside to speak with Salma Mohammed, the school's social researcher. She has known Noor for the year and a half she has been at the school.\n" +
                "The number of Iraqis with disabilities has grown significantly after decades of conflict, starting with the Iran-Iraq War in the 1980s, she says. One of Noor's best friends, Hajar, is also a paraplegic. She was wounded when a mortar landed in her front yard.\n" +
                "Mohammed believes Iraqis have become more aware of the disabled, but the country still lacks the resources to afford them a normal life.\n" +
                "\"To be honest with you, all these children need to be treated abroad,\" she says.\n" +
                "Mohammed shows me Noor's report card. She is under par for a second-grader. Her teachers say she doesn't understand instruction as well as she should.\n" +
                "Beyond the physical limitations, Mohammed fears Noor is developing psychological problems -- that she suffers from depression and loss of confidence and self-esteem.\n" +
                "\"I told the family they should have left Noor in America,\" Mohammed says. \"They said, 'Yes, we regret it.'\n" +
                "\"I've been watching her case,\" Mohammed says. \"She is not progressing.\"\n" +
                "Two mamas\n" +
                "In the afternoon, Fatima sits down on the floor to make dolmas, vine leaves stuffed with a mixture of lamb and rice.\n" +
                "Noor sits in her wheelchair watching her work.\n" +
                "Noor, center, attends a public school for disabled children in Baghdad. Her teachers say she is quiet and reserved, and they fear that she may be developing mental and emotional problems.\n" +
                "David S. Holloway/CNN\n" +
                "\"It's OK,\" Zainab reassures her.\n" +
                "\"So Noor calls you mama?\" I ask Zainab.\n" +
                "\"Yes,\" she says. \"And Fatima, too.\"\n" +
                "They are the two women who took over as primary caregivers after Soad died in 2008. She underwent routine gallbladder surgery but did not survive the complications afterward.\n" +
                "The family blames Iraq's medical system. They say all the good doctors fled the country once the war began. They also say Soad died of a broken heart: Her son Bashar was still behind bars and Noor was struggling.\n" +
                "\"My mother did everything for her. She spoiled her,\" says Hanan, another one of Haider's sisters. \"After she died, it was different. We all have our own lives, our own children. There's less and less time for Noor.\"\n" +
                "Soad worried constantly about her granddaughter's future. She made the family vow to never forsake Noor.\n" +
                "Zainab stepped into Soad's role. Noor,  she says, is keenly aware of her situation.\n" +
                "She asks Noor sometimes: \"Who is your real mother?\"\n" +
                "\"Iman,\" Noor answers.\n" +
                "\"She didn't want me,\" Noor answers. \"She left me. ... You are my mother.\"\n" +
                "As she grows older, she is more and more conscious of Zainab and Fatima having to help her urinate by inserting a catheter.\n" +
                "They are still using the plastic tubes that Childspring gave them. They use each one three times but only have about 100 left and don't know if they are available in Baghdad. Sometimes, she suffers from urinary tract infections, which have to be treated with antibiotics.\n" +
                "Typical of spina bifida children, Noor also cannot defecate by herself. Increasingly, it's a source of embarrassment.\n" +
                "One time, Fatima asked a local neighborhood boy to run to the store to buy the pads that Noor wears to school in case of uncontrolled leakage. Noor was upset. She didn't want an outsider to know.\n" +
                "Sometimes, Noor breaks down in tears when she can no longer stand to be around normal children. She looks into her aunts' eyes as if to get affirmation of her situation.\n" +
                "She once had a fight with one of her cousins who told her: \"I am better than you. I can walk.\"\n" +
                "Noor screamed back: \"Some day, I will walk.\"\n" +
                "She constantly tells Zainab: \"I want to walk like the other kids.\"\n" +
                "\"Inshallah,\" Zainab answers. God willing. Zainab doesn't know how else to answer.\n" +
                "\"When she sees other children, she gets frustrated, depressed,\" Zainab says. \"It eats at her from the inside. She knows her mother didn't want her. This affected her greatly.\"\n" +
                "At night, Noor often sleeps next to Zainab, holding onto her tightly. \"I am sad,\" she tells her.\n" +
                "Recently, Zainab told Noor she is engaged to be married and might soon move away to be with her husband in Dubai.\n" +
                "\"I'm honestly not sure how Noor will feel when I leave,\" she says.\n" +
                "She has thought about the possibility of taking Noor with her, if they are able to overcome immigration hurdles. But that may prove to be difficult.\n" +
                "\"She is closer than my own soul,\" Zainab says. \"I'm more than a mother to her.\"\n" +
                "Noor's grandfather worries, too. He is 65 and fears he will die soon. He clutches his prayer beads and watches his granddaughter put Zainab's black flats on her hands and crawl on two limbs inside the house -- it is too much to maneuver a wheelchair in a house not made for one.\n" +
                "Fatima thinks the hardest part will be for Noor to watch her cousins get married and start their own families. That's what's expected of all women in Noor's circle.\n" +
                "But no one in Iraq will marry Noor, Fatima says. She will die alone.\n" +
                "Holding onto hope\n" +
                "Noor is still young, but Fatima and Zainab are certain she understands life way beyond her years.\n" +
                "At times, she asks her family: \"Why did you bring me back from America?\"\n" +
                "Noor uses a wheelchair outside the house. Inside, she slips her hands into her aunt's shoes and crawls on the floor. It's easier that way in a house that's not wheelchair-friendly.\n" +
                "David S. Holloway/CNN\n" +
                "It was the trip of a lifetime for Noor and one that taught her family to hope.\n" +
                "Haider knows now, after his brief visit to Atlanta, what life might have been for his daughter. And what it is destined to be in Baghdad.\n" +
                "Zainab and others in the family hold onto a belief, however false, that if only Noor can return to America, she will be able to walk again. That she has a chance to be normal.\n" +
                "If nothing else, they hope someone in America will reach out to them again.\n" +
                "She needs a place of stability, Haider says. \"Here, you live in constant fear in your heart and you don't know what to expect.\"\n" +
                "Haider's father, Khalaf, says he is grateful for everything the Americans did for Noor. He says Noor still needs their help.\n" +
                "He fires off a series of questions: Why didn't anyone from Childspring keep in touch with them? Why doesn't the group connect the family with good doctors or send them catheters or a new wheelchair? Who will pay for Noor's brain scans or surgery if she needs it?\n" +
                "What if his granddaughter -- as Dr. Saad Nasser said during their last visit to his clinic -- dies?\n" +
                "I sense desperation in his voice. It reminds me of the frustration I've heard from other Iraqis who feel the United States abandoned them when it withdrew all its troops in 2011.\n" +
                "It becomes increasingly clear that Noor and her family see me as the arrival of help from America. In their predicament, in this war-torn country, they have nowhere else to place their hope.\n" +
                "Zainab tells me Noor's demeanor has been different since she learned I would be visiting.\n" +
                "I explain to Haider's father that I am here to tell Noor's story. I can take their message back to America, but I am not the bearer of miracles.\n" +
                "A parting gift\n" +
                "\"A woman,\" she says. Her aunts giggle.\n" +
                "\"And a doctor,\" she says. \"I want to treat people so they don't die.\"\n" +
                "\"What's your favorite show on TV?\" I ask.\n" +
                "\" 'Tom and Jerry,' \" she says. And, after a prompt from Fatima, she adds: \" 'SpongeBob.' \"\n" +
                "It is one of my last visits to Noor's house before I return to Atlanta.\n" +
                "She has a present for me, wrapped in red and gold foil paper. She sits next to me on the floor, watching intently as I open it. It is a porcelain elephant. She wanted to give me something that reminded me of my native India.\n" +
                "\"To the friend that brings happiness to an Iraqi family and became a part of them (Moni),\" the card says. \"We wish you progress and success in your work.\"\n" +
                "The card is signed \"Baby Noor.\"\n" +
                "I try, but fail, to stop the tears.\n" +
                "Epilogue\n" +
                "I promised Noor's family that when I returned home, I would reach out to some of the people who played a role in their lives in America.\n" +
                "I brought back with me copies of Noor's last CT scans, done about two years ago. I e-mailed them to Roger Hudgins, the doctor who operated on Noor.\n" +
                "He tells me they looked \"pretty darn close\" to what they looked like when she left. I breathe a sigh of relief.\n" +
                "But he says Noor's headaches bother him. The fluid in her brain could be causing them. If she were in America, she'd be referred to a pediatric neurologist for headaches. She ought to be properly examined by a neurosurgeon, he says.\n" +
                "What worries him more is Noor's bladder function. She runs a high risk of complications from a urinary tract infection.\n" +
                "\"That's most likely to take her life,\" he says.\n" +
                "CNN's Moni Basu shares a moment with Noor, who says she wants to be a doctor when she grows up. She says she wants to treat people so they don't die.\n" +
                "David S. Holloway/CNN\n" +
                "It's what used to kill spina bifida patients in this country until advancements in urological care extended their lives. It's as though Noor is living two decades ago.\n" +
                "Without her bladder functioning properly, she could get urinary sepsis that could damage the organs and end in renal failure. She needs to be drinking lots of water and taking stool softeners, Hudgins says. I can't remember seeing Noor drink water except for a few sips with lunch.\n" +
                "I also sent Hudgins a photograph of Noor. He saved her life. Surely he wanted to see what she looks like now.\n" +
                "\"What I saw in that picture was a very pretty girl,\" he tells me. If only she could learn the skills to adapt to her condition.\n" +
                "The family showed enormous courage to bring Noor to America, Hudgins says. He hopes they have the fortitude now to continue her care.\n" +
                "\"But that's hard,\" he says. Even for families here in America who have access to the best medical resources.\n" +
                "Christina Porter, program director of Childspring, says the organization is willing to help get more supplies to Noor's family and possibly arrange for her to be examined by specialists in Iraq. That is heartening news, but I know acts that seem simple enough to do in America can turn out to be insurmountable challenges in Iraq.\n" +
                "Jeff Morgan, the soldier who set Noor's journey in motion, says everyone knew coping with life would never be easy for Noor.\n" +
                "\"I'm glad to hear she is alive,\" he says. \"I still believe it was the right thing to do.\"\n" +
                "Then he tells me something unexpected.\n" +
                "There were a few soldiers who did not approve of Morgan's efforts to shuttle Iraqis to America in the middle of a war. Some of the Georgia soldiers had lost several of their friends. They were angry, bitter. They did not think it was worth taking the risk for a family they suspected of anti-American activity.\n" +
                "In the emotional disconnect of war, they saw another solution.\n" +
                "\"You should have just shot her,\" one platoon sergeant told Morgan.\n" +
                "It's a thought that might shock those who gave Noor the gift of life, among them another woman who, for a time, acted as her mother. I promised Noor's family I would go see her after my trip to Baghdad.\n" +
                "I'd last seen Nancy Turner just days before she boarded a flight bound for Kuwait, with Noor in her arms. Nancy cried the entire way back to Atlanta. How could she not? She'd cared for Noor as though she were her own daughter.\n" +
                "She welcomes me into her home in the Atlanta suburb of Alpharetta. She is eager to see the photos and video of Noor I've brought. It is as though a long lost child has finally returned home.\n" +
                "She reaches for tissues as images of Noor flash before her, as she hears Noor talk and sing and sees her in a classroom.\n" +
                "\"Those eyes, that smile,\" Nancy says, glad to learn that Noor can read and write and that she appears to be loved by her family.\n" +
                "Nancy remembers when the baby fussed, she'd stand with her in front of a mirror.\n" +
                "\"She could see herself and see me and she would laugh and that's a memory I will always hold dear,\" Nancy says.\n" +
                "\"She changed me. I don't think of myself as a patient person ... but Noor required patience because of the care she needed and it was in providing that care and taking the time instead of rushing through that I learned something about myself.\"\n" +
                "In her house, Nancy displays a portrait of herself and her husband with Noor, Haider and Soad.\n" +
                "There are other reminders as well.\n" +
                "She wears a gold necklace bearing four lockets -- a heart for love, a cross for her Christian faith, a sand dollar for her fondness of the beach and an eye to ward off evil. Soad gave her the eye when she met her in Kuwait to take Noor back to Baghdad. On Nancy's silver charm bracelet, there's a pair of baby shoes that a friend gave her when she became Noor's temporary mother.\n" +
                "In her kitchen, the cream-colored Corian counter by the stove has a deep crack. She says it was because of the weight of the heavy pots Soad liked to use. I remember Noor's grandmother loved to cook. Soad and I had many conversations about the similarities between Iraqi and Indian cuisine, and I brought Indian-style biryani to a dinner Nancy hosted for Soad to taste.\n" +
                "Noor's father and uncle keep pigeons on the roof of their house. They are a way to relieve tension after a hard day's work selling fruits and vegetables at a roadside stand.\n" +
                "David S. Holloway/CNN\n" +
                "Even though the rest of her house is in immaculate condition, Nancy never fixed the countertop. She's never stopped thinking about the life Noor is leading in Iraq.\n" +
                "It was never anyone else's duty to take over parenting of that child, she says. Noor has a father, a family.\n" +
                "But the ties that were forged cannot be severed.\n" +
                "Nancy tells me she is glad I was able to reconnect with the family. She says she will get in touch with Childspring about possibly helping Noor again.\n" +
                "I always saw Noor's story as a reflection of the larger issues at stake in Iraq. American military involvement is over, but what is our connection now with a people struggling to stand up on their own?\n" +
                "As I drive back home, Nancy's words about Noor and her family repeat in my head: \"Our responsibility was and is to walk beside them.\"\n" +
                "How this story was reported\n" +
                "CNN's Moni Basu first met Noor and her family in December 2005 when U.S. soldiers came upon them during a routine raid. Basu was embedded with the Army unit to which those soldiers belonged and told the story of Baby Noor for The Atlanta Journal-Constitution.\n" +
                "In February, she returned to Iraq with CNN's David S. Holloway to document Noor's life now, more than seven years after the child was shuttled to the United States for life-saving surgery.\n" +
                "Basu and Holloway spent time with Noor and her family in southwest Baghdad over five days. The family had previously paid a price for its association with Americans, and CNN could not photograph certain scenes in the story for security reasons.\n" +
                "As conservative Shiites, many of the women in the family as well as Noor's teachers declined to be interviewed on camera.\n" +
                "Most of the interviews in Baghdad were conducted in Arabic with the help of CNN producer Mohammed Tawfeeq, who served as a translator.\n" +
                "Additional credits: Sean O'Key, Bryan Perry, Lee Smith\n" +
                "How you can help Baby Noor\n" +
                "Childspring International, the Atlanta-based Christian charity that sponsored her stay in the United States in 2006, said it was setting up a special Baby Noor Fund so it could channel donations specifically to Noor's family.\n";
        artifact.getContent().setDocExtractedText(text.getBytes());

        Collection<Sentence> sentences = extractor.extractSentences(artifact);

        Iterator<Sentence> iterator = sentences.iterator();

        Sentence sentence = iterator.next();
        assertEquals("urn:sha256:abcd:0000000000000172:0000000000000000", sentence.getRowKey().toString());
        assertEquals("urn:sha256:abcd", sentence.getData().getArtifactId());
        assertEquals(Long.valueOf(0), sentence.getData().getStart());
        assertEquals(Long.valueOf(172), sentence.getData().getEnd());
        assertEquals("Iraq's Baby Noor: An unfinished miracle\n" +
                "American soldiers plucked the child from her Iraqi home at the height of the war and brought her to America for life-saving surgery.", sentence.getData().getText());
    }
}
