This is the dataset used to create the demo videos as well as a few articles, associated with different topics, to create a mini dataset for people to use live during GeoINT.  All the data was cleaned up and should be properly displayed within the detail pane.  To use this dataset you will need to download the dataset zip, the ontology zip, and the dictionary zip.  When everything is downloaded all of the people, places, attacks, and organizations will be pre-resolved.

## Data
Description of information held within each of the topics for searching and using while doing a live demo.

[Terrorist Colombia](https://s3.amazonaws.com/RedDawn/DataSets/TerroristColombia.zip)

[Terrorist Colombia - without Live Demo Data](https://s3.amazonaws.com/RedDawn/DataSets/TerroristColombia-withoutLive.zip)

### Colombian Terrorist Organizations
For each organization there is a terrorist profile of important information, attacks they are responsible for within the last 5 years, a video and some pictures for the group, and articles about the impacts of the group and its members.

**Eco-Terrorist Organization (ETO)**
Data focused around a an ecoterrorist group located in Peru, Ecuador, and Colombia that is responsible for bombings of
buildings associated with companies with a large environmental impact.

**Guerrilla Terrorist Organization (GTO)**
Information centered around a drug and human trafficking organization focused in Colombia.  This organization is the cause of political bombings, kidnappings, and the largest cocaine providers in the region.

**Shining Star (SS)**
Articles and information about the group which is the largest collective support group for the Communist Party in Peru.  They are responsible for political bombings and kidnappings in Peru with a focus of rising the Communist Party to central power in the government of Peru.

**Attack**
News reports of a bus hijacking in Colombia where a bus of Hong Kong tourists were kidnapped by a former police officer, Luis Mendoza.  He took control of the bus and shot and killed 8 of the individuals as well as injuring 11 others before the Colombian police killed him.  He has ties to the leader of the GTO and is an assumed member of the group.

### Live Demo Data:
This dataset contains 33 documents, 35 images, and 6 videos surrounding one of the topics listed out below.  None of the entities in these documents have been resolved unless they have matching words to the dictionary files for the Terrorist Attack dataset.

**Barack Obama, Syria, Tampa Bay Police Department, U.S. Air Force, U.S. Army, U.S. Coast Guard, U.S. Marine Corps, U.S. Navy and Rebecca Sedwick (a local Lakeland girl who committed suicide because of cyber bullying)**


## Ontology file
The ontology file includes the addition of events, events/attack, and organization/terrorist organization to the base ontology.  Properties of events includes dates and locations; attacks can also have fatalities, injured, targeted, attack type, and relationship to an organization that carries out the attack. Terrorist organization can have an added abbreviation, year formed, alias, strength, classification, financial sources, and related groups, relationship to the key leaders and locations.  This ontology file does change the colors for each of the types of entities so that it is more distinguishable which entity is represented.

[Zipfile of all ontology](https://s3.amazonaws.com/RedDawn/DataSets/dev-ontology.zip)

[dev.owl](https://s3.amazonaws.com/RedDawn/DataSets/dev.owl)

## Dictionary Files
This contains the dictionaries files that will resolve all of the terrorist organizations, people and countries associated with the terrorist organizations, and the attacks to entities upon running. Any entities for the live demo dataset have not been resolved unless they happen to coincide with the other desired entities.

[Dictionaries](https://s3.amazonaws.com/RedDawn/DataSets/dictionaries.zip)