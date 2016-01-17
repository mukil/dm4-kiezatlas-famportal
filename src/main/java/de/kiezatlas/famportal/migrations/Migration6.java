package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.plugins.workspaces.service.WorkspacesService;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Migration6 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger log = Logger.getLogger(getClass().getName());


    @Inject private WorkspacesService workspaceService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        // 1) re-assign all their children to these category parents
        Topic familieUndPartnerschaft = dms.getTopic("uri", new SimpleValue("famportal.category-e99c03aa-a0cb-44e4-a518-3ab4b7062679-de_DE-1"));
        Topic beratungUndUnterstützung = dms.getTopic("uri", new SimpleValue("famportal.category-a0e061b5-83b0-4c64-818c-a53c3137c2c9-de_DE-1"));
        Topic ausbildungUndBeruf = dms.getTopic("uri", new SimpleValue("famportal.category-20186c9c-a413-421f-8f1e-a80fb266d79f-de_DE-1"));
        Topic lebenslagen = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-bb19be3d-f705-4516-83f3-517ca277a73f-de_DE-1"));
        Topic familiePlus = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-b67cf945-4ffb-4916-8f69-2e25b271a9b1-de_DE-1"));
        // ## Ordnungszahlen anpassen (Ehe, Eheähnliche Gemeinschaft, Lebenspartnerschaft, Trennung/Scheidung)

        // 3) Insert new categories into the famportal category tree
        for (Topic topic : dms.getTopics("famportal.category", 0).getItems()) {

            // --- Themen ---

            // Move all bundFuersLeben "Themen" subcategories
            if (topic.getUri().equals("famportal.category-68235a12-f151-494c-8e5d-c2dd5492449a-de_DE-1")) { //
                topic.loadChildTopics();
                // Move, Ehe, Eheähnliche G., Lebenspartnerschaft, Trennund und Scheidung
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + familieUndPartnerschaft.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                familieUndPartnerschaft.update(category);
                // Move all lebenMitKindern-Themen subcategories
            } else if (topic.getUri().equals("famportal.category-62d6d43c-a80e-4a89-8eb6-fb4d88bbaf80-de_DE-1")) { //
                topic.loadChildTopics();
                // Re-assigning 11 Topics
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + familieUndPartnerschaft.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                familieUndPartnerschaft.update(category);
            } else if (topic.getUri().equals("famportal.category-9ee03c32-24bb-49ad-979e-62c422fc26df-de_DE-1")) {
                // todo: Trennung und Scheidung-> 8
                // Rename to "Trennung und Scheidung"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Trennung und Scheidung"));
            } else if (topic.getUri().equals("famportal.category-2fb19290-eda5-4a4a-8418-e35f944775bb-de_DE-1")) {
                // Rename to "Kinderbetreuung und Kita"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Kinderbetreuung und Kita"));
            } else if (topic.getUri().equals("famportal.category-74fbaa2c-8b79-40b6-b262-cbf4735c6f01-de_DE-1")) {
                topic.loadChildTopics(); // Unterstützung im Alltag
                // Re-assigning two topics
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + beratungUndUnterstützung.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                beratungUndUnterstützung.update(category);
            } else if (topic.getUri().equals("famportal.category-6f01c6f8-ccc7-4e37-a751-34590de758de-de_DE-1")) {
                topic.loadChildTopics(); // Notlagen
                // Re-assigning two topics
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + beratungUndUnterstützung.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                beratungUndUnterstützung.update(category);
            } else if (topic.getUri().equals("famportal.category-62fd8f06-cee6-47c2-9fe2-de152ee51b4c-de_DE-1")) {
                // delete "Migration" category "Sprachförderung"
                log.info("### Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();
            } else if (topic.getUri().equals("famportal.category-3b1965b5-2e74-4a07-bf20-7b1866833bc0-de_DE-1")) {
                // delete "Migration" category "Einbürgerung"
                log.info("### Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();
            } else if (topic.getUri().equals("famportal.category-04e58d69-f48b-4847-b60f-9b3c4b0861f8-de_DE-1")) {
                topic.loadChildTopics();
                // Re-assigning two topics
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + beratungUndUnterstützung.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                beratungUndUnterstützung.update(category);
                // Delete and re-assign topcis of "Wohnen"
            } else if (topic.getUri().equals("famportal.category-2d843bbd-25eb-4c15-b69b-bd85c1765d00-de_DE-1")) {
                topic.loadChildTopics();
                // Re-assigning two topics of "Wohnen"
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("### Adding References on " + beratungUndUnterstützung.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                beratungUndUnterstützung.update(category);
            } else if (topic.getUri().equals("famportal.category-039457f1-fedd-4aea-8549-47edcdbf72e6-de_DE-1")) {
                // delete category "Nachbarschaft"
                log.info("### Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();
            } else if (topic.getUri().equals("famportal.category-b5d5cb63-dfcc-4cdf-a1ee-5096a811053e-de_DE-1")) {
                // Introduce new category "Sucht" to "Gesundheit"
                topic.loadChildTopics();
                // ..) Create new child topics model
                TopicModel suchtModel = createFamportalCategoryTopicModel("Sucht", 25,
                        "category-e048b3b6-cdef-4566-9eef-9cad8bf6edc1-de_DE-1");
                // ..) Add new composite topic as child topic to our current topic
                // Topic sucht = dms.createTopic(new TopicModel(childsTopicModel));
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                categoryTopicModel.add("famportal.category", suchtModel);
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                topic.update(category);
                // Moving subtopics of "Nach der Schule"
            } else if (topic.getUri().equals("famportal.category-b5cbe927-3a68-4e82-b916-23e913321162-de_DE-1")) {
                topic.loadChildTopics();
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                for (Topic el : topic.getChildTopics().getTopics("famportal.category")) {
                    log.info("Adding References on " + ausbildungUndBeruf.getSimpleValue() + " to " + el
                            .getSimpleValue());
                    categoryTopicModel.addRef("famportal.category", el.getId());
                }
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
                ausbildungUndBeruf.update(category);
            } else if (topic.getUri().equals("famportal.category-2d179e10-5e95-409e-8f65-fe492b6422cf-de_DE-1")) {
                removeParentalReference(topic, "famportal.category-b67338c6-9346-47b1-9fed-36e33a6b698d-de_DE-1");
                assignTopicNewParent(topic, ausbildungUndBeruf);
            } else if (topic.getUri().equals("famportal.category-8462e4da-0d9c-49a6-838d-6a5fa21669e8-de_DE-1")) {
                // delete "Kinderbetreuung"
                log.info("## Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();
            } else if (topic.getUri().equals("famportal.category-973782a5-e91f-44af-b313-22f6c736d4c2-de_DE-1")) {
                // Create new category "Jugendarbeit" to "Freizeit und Kultur"
                TopicModel jugendArbeitModel = createFamportalCategoryTopicModel("Jugendarbeit", 05,
                        "category-f661d708-4f58-412b-90a1-e7815f0a820b-de_DE-1");
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                categoryTopicModel.add("famportal.category", jugendArbeitModel);
                TopicModel categoryModel = new TopicModel("famportal.category", categoryTopicModel);
                topic.update(categoryModel);
                // Create new category "Suchterkrankung" to "Lebenslagen"
                TopicModel suchterkrankung = createFamportalCategoryTopicModel("Suchtgef\u00e4hrdung und " +
                        "Suchterkrankung", 75, "category-36085352-12db-45da-88dd-2afa27216838-de_DE-1");
                ChildTopicsModel suchtCategoryModel = new ChildTopicsModel();
                suchtCategoryModel.add("famportal.category", suchterkrankung);
                TopicModel category = new TopicModel("famportal.category", suchtCategoryModel);
                lebenslagen.update(category);
                // Create new category "Neu in der Stadt" in "Lebenslagen"
                TopicModel neuInDerStadt = createFamportalCategoryTopicModel("Neu in der Stadt", 105,
                        "category-f8d0fd7d-48be-4e69-bcff-98da03d82ecf-de_DE-1");
                ChildTopicsModel neuTopicModel = new ChildTopicsModel();
                neuTopicModel.add("famportal.category", neuInDerStadt);
                TopicModel neuModel = new TopicModel("famportal.category", neuTopicModel);
                lebenslagen.update(neuModel);
                // Create new category "Todesfall" in "Lebenslagen"
                TopicModel todesfall = createFamportalCategoryTopicModel("Todesfall", 115,
                        "category-a7b08000-e727-4bde-bdb3-7b552679bc87-de_DE-1");
                ChildTopicsModel todesfallModel = new ChildTopicsModel();
                todesfallModel.add("famportal.category", todesfall);
                TopicModel todesfallTopicModel = new TopicModel("famportal.category", todesfallModel);
                lebenslagen.update(todesfallTopicModel);

                // --- Lebenslagen ---

            } else if (topic.getUri().equals("famportal.category-8c24a931-4844-433e-96f6-673d0def3924-de_DE-1")) {
                // Rename to "Bund fürs Leben eingehen" .. umbenennen
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Bund f\u00fcrs Leben"));
                // Remove reference to "Familie sein"
                removeParentalReference(topic, "famportal.category-99bc3d7e-9d0b-43f2-b9ed-960b24c85cef-de_DE-1");
                // Bunds fürs Leben in "Lenbeslagen" verschieben
                assignTopicNewParent(topic, lebenslagen);

                // Delete "Mutterschaft", "Vaterschaft", "Frühe Elternschaft"," Späte Elternschaft" unterhalb von
                // Familie sein
            } else if (topic.getUri().equals("famportal.category-cfb6c7ee-038f-4c47-bbf7-7ce8d67d36a2-de_DE-1") ||
                    topic.getUri().equals("famportal.category-f756ac6d-3946-49bb-8cf1-8d16b95affc1-de_DE-1") ||
                    topic.getUri().equals("famportal.category-714b8306-9f6e-4d06-8477-39bb2b6d00d1-de_DE-1") ||
                    topic.getUri().equals("famportal.category-188fff80-be49-4380-9f81-ee4dc8480d29-de_DE-1")) {
                log.info("## Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();

                // Rename and Move to "Lebenslagen"
            } else if (topic.getUri().equals("famportal.category-cfff96f9-95d0-4c19-a86a-a3fe7ca75df0-de_DE-1")) {
                // Rename and Move to "Alleinerziehend und Einelternfamilie"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name",
                        "Alleinerziehend und Einelternfamilie"));
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-11a80e7a-5b29-4d0d-8bfc-8591d5939dec-de_DE-1")) {
                // Rename and Move Trennung und Scheidung
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Trennung und Scheidung"));
                // Delete reference to "bund fuers leben"
                removeParentalReference(topic, "famportal.category-8c24a931-4844-433e-96f6-673d0def3924-de_DE-1");
                // Add reference to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-f8b020ee-d31e-4ea8-8fb0-0ce30b0e9236-de_DE-1")) {
                // Rename to "Arbeitslosigkeit"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Arbeitslosigkeit"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-d8a993b2-8dd1-4ab5-8033-d9a542ce01ac-de_DE-1")) {
                // Rename to "Schule"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Schule"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-56db817c-e57d-41a5-8b9f-a9ddfb789a28-de_DE-1")) {
                // Rename to "Ausbildung"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Ausbildung"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-cf95d10a-7f07-4953-bd7b-c24dd9870d9a-de_DE-1")) {
                // Rename to "Übergang Schule/Beruf" // TODO: ### Unicode "Ü"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "\u00dcbergang " +
                        "Schule/Beruf"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-ebc25c74-76a7-419c-a455-554a7c565a8c-de_DE-1")) {
                // Rename to "Sich weiterbilden" // TODO: ### Unicode "Ü"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Weiterbildung"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-bd769edb-2ff1-456d-b57e-8f81d1a4fbdf-de_DE-1")) {
                // Rename to "Studium"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Studium"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-a73511a2-64f4-437c-accb-b4955b512865-de_DE-1")) {
                // Rename to "Leben mit Krankheit"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Leben mit Krankheit"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-0bd66aef-987e-4b16-a38e-98236f1debe8-de_DE-1")) {
                // Rename to "Vorsorge von Krankheit, Unfall, Tod"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Vorsorge von Krankheit, Unfall, Tod"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
            } else if (topic.getUri().equals("famportal.category-fc8c1bac-473d-4d57-8e0f-51161cc1ddaa-de_DE-1")) {
                // Rename to "Im Ruhestand"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Ruhestand"));
                // Move to "Lebenslagen"
                assignTopicNewParent(topic, lebenslagen);
                // Remove reference to "Ruhestand" and add one to "Lebenslagen" instead
                Topic erbenUndVererben = dms.getTopic("uri", new SimpleValue("famportal" +
                        ".category-7b0a26aa-9338-4d2c-a50c-99607b0eb059-de_DE-1"));
                removeParentalReference(erbenUndVererben, topic.getUri());
                assignTopicNewParent(erbenUndVererben, lebenslagen);
                // Delete Famportal category (.., .., ,.., ..)
            } else if (topic.getUri().equals("famportal.category-f47872c9-ee93-425c-a1d8-5ccc0eac8e00-de_DE-1") ||
                    topic.getUri().equals("famportal.category-c718ed0d-5f5c-46c7-8c67-55c441027e4f-de_DE-1") ||
                    topic.getUri().equals("famportal.category-3aa7ab6a-2f7b-4931-90ad-fc7ed7ce193d-de_DE-1") ||
                    topic.getUri().equals("famportal.category-dca089c2-8be0-459d-9aff-4e57631c4747-de_DE-1")) {
                log.info("### Deleting Famportal Category Topic " + topic.getSimpleValue());
                topic.delete();

                // Move "Wohnungsnot", "Opfer von Gewalttaten", "Kinderwunsch", "Schwangerschaft", "Geburt", Das
                // erste Jahr", "Kind sein", "Freiwillig tätig sein", "Vereinbarkeit Familie und Beruf"
                // "Gesund leben" to "Lebenslagen"
            } else if (topic.getUri().equals("famportal.category-c7433ae4-de8b-4c17-b729-e79b1d85844b-de_DE-1") ||
                    topic.getUri().equals("famportal.category-dd609757-d742-407f-bf80-76cd56d94986-de_DE-1") ||
                    topic.getUri().equals("famportal.category-4c37f8fc-eea6-4ae7-a090-7876fad07ae6-de_DE-1") ||
                    topic.getUri().equals("famportal.category-e1b59390-585e-40a5-9b26-5cedfc5a4385-de_DE-1") ||
                    topic.getUri().equals("famportal.category-5916d5c7-7a5f-476e-9b76-a25348ee2c8d-de_DE-1") ||
                    topic.getUri().equals("famportal.category-9f29b621-6a76-44b8-a514-77fa046f79b5-de_DE-1") ||
                    topic.getUri().equals("famportal.category-aafa2c9c-a978-4a92-bdd0-806e2f03b081-de_DE-1") ||
                    topic.getUri().equals("famportal.category-80943438-5e77-4257-8480-e9ea9ac01cd8-de_DE-1") ||
                    topic.getUri().equals("famportal.category-e55a5780-e6a1-4487-92a4-e96cfa4945bb-de_DE-1") ||
                    topic.getUri().equals("famportal.category-85d337e9-a06f-46c3-9b8e-c41e4082cbb6-de_DE-1") ||
                    topic.getUri().equals("famportal.category-650f506b-b6c6-4e62-8769-30658b1dcb6e-de_DE-1") ||
                    topic.getUri().equals("famportal.category-7e7b37b8-4f2b-478f-9a0a-b9580462cb3a-de_DE-1") ||
                    topic.getUri().equals("famportal.category-7b0a26aa-9338-4d2c-a50c-99607b0eb059-de_DE-1") ||
                    topic.getUri().equals("famportal.category-9b34b8d5-e1ea-4808-a672-b0da4d170638-de_DE-1") ||
                    topic.getUri().equals("famportal.category-f55e2aab-6a08-43e8-ab32-9e3eebd52398-de_DE-1")) {
                log.info("### Move Famportal Category " + topic.getSimpleValue() + " to " + lebenslagen
                        .getSimpleValue());
                assignTopicNewParent(topic, lebenslagen);

                // --- Familie+ ---
            } else if (topic.getUri().equals("famportal.category-b67cf945-4ffb-4916-8f69-2e25b271a9b1-de_DE-1")) {
                // 1) Create new categories to "Familie+"
                TopicModel babys = createFamportalCategoryTopicModel("Neugeborene und Babys", 10,
                        "category-da6043d2-f172-4879-81a2-3427e586d249-de_DE-1");
                ChildTopicsModel babyCategoryModel = new ChildTopicsModel();
                babyCategoryModel.add("famportal.category", babys);
                TopicModel babyCategory = new TopicModel("famportal.category", babyCategoryModel);
                topic.update(babyCategory);
                // 2) Create "Vorschulkinder cat
                TopicModel kinder = createFamportalCategoryTopicModel("Kinder und Vorschulkinder", 20,
                        "category-80181a43-6a65-456c-9541-a395960fa869-de_DE-1");
                ChildTopicsModel kinderCategoryModel = new ChildTopicsModel();
                kinderCategoryModel.add("famportal.category", kinder);
                TopicModel kinderCategory = new TopicModel("famportal.category", kinderCategoryModel);
                topic.update(kinderCategory);
                // 3) Create "Junge Erwachsene"
                TopicModel jungeErwachseneModel = createFamportalCategoryTopicModel("Junge Erwachsene", 50,
                        "category-dc1024ce-d96b-43fa-8efc-6bc24e67772b-de_DE-1");
                ChildTopicsModel jungeErwachseneCategoryModel = new ChildTopicsModel();
                jungeErwachseneCategoryModel.add("famportal.category", jungeErwachseneModel);
                TopicModel jungeErwachseneCategory = new TopicModel("famportal.category", jungeErwachseneCategoryModel);
                topic.update(jungeErwachseneCategory);
                // 4) Create "Mütter"
                TopicModel mutterModel = createFamportalCategoryTopicModel("M\u00fctter", 70,
                        "category-4b4ee9be-0aae-4bfe-ad24-1cdb83f5e318-de_DE-1");
                ChildTopicsModel mutterModelCategoryModel = new ChildTopicsModel();
                mutterModelCategoryModel.add("famportal.category", mutterModel);
                TopicModel mutterCategory = new TopicModel("famportal.category", mutterModelCategoryModel);
                topic.update(mutterCategory);
                // 5) Create "Väter"
                TopicModel vaterModel = createFamportalCategoryTopicModel("V\u00e4ter", 80,
                        "category-60ff6f60-0613-442c-83f1-7623248e8279-de_DE-1");
                ChildTopicsModel vaterModelCategory = new ChildTopicsModel();
                vaterModelCategory.add("famportal.category", vaterModel);
                TopicModel vaterCategory = new TopicModel("famportal.category", vaterModelCategory);
                topic.update(vaterCategory);
                // 6) Create "Pflegeeltern"
                TopicModel stepParents = createFamportalCategoryTopicModel("Pflegeeltern", 90,
                        "category-47b0c760-b98d-4a88-9f5a-1cf7041f2723-de_DE-1");
                ChildTopicsModel stepParentsModelCategory = new ChildTopicsModel();
                stepParentsModelCategory.add("famportal.category", stepParents);
                TopicModel stepParentsCategory = new TopicModel("famportal.category", stepParentsModelCategory);
                topic.update(stepParentsCategory);
                // 7) Create "Alleinerziehend"
                TopicModel alleinerziehend = createFamportalCategoryTopicModel("Alleinerziehende", 100,
                        "category-7d9766a0-58de-4173-b457-c0b8d027f9bb-de_DE-1");
                ChildTopicsModel alleinerziehendModelCategory = new ChildTopicsModel();
                alleinerziehendModelCategory.add("famportal.category", alleinerziehend);
                TopicModel alleinerziehendCategory = new TopicModel("famportal.category", alleinerziehendModelCategory);
                topic.update(alleinerziehendCategory);
                // 8) Create "Menschen in Not"
                TopicModel menschenNot = createFamportalCategoryTopicModel("Menschen in Not", 110,
                        "category-f0daf9b2-da43-4470-945f-21cb36f86930-de_DE-1");
                ChildTopicsModel menschenNotModelCategory = new ChildTopicsModel();
                menschenNotModelCategory.add("famportal.category", menschenNot);
                TopicModel menschenNotCategory = new TopicModel("famportal.category", menschenNotModelCategory);
                topic.update(menschenNotCategory);
                // 9) Create "Patchworkfamilien"
                TopicModel patchworkfamilien = createFamportalCategoryTopicModel("Patchworkfamilien", 120,
                        "category-f916d36b-cef6-42bd-b260-d27e6e4029a6-de_DE-1");
                ChildTopicsModel patchworkfamilienCategoryModel = new ChildTopicsModel();
                patchworkfamilienCategoryModel.add("famportal.category", patchworkfamilien);
                TopicModel patchworkCategory = new TopicModel("famportal.category", patchworkfamilienCategoryModel);
                topic.update(patchworkCategory);
                // 10) Create "Regenbogenfamilien"
                TopicModel regenbogenfamilien = createFamportalCategoryTopicModel("Regenbogenfamilien", 130,
                        "category-57251098-0ae4-49dd-9fab-313ec83c97b7-de_DE-1");
                ChildTopicsModel regenbogenfamilienModel = new ChildTopicsModel();
                regenbogenfamilienModel.add("famportal.category", regenbogenfamilien);
                TopicModel regenbogenCategory = new TopicModel("famportal.category", regenbogenfamilienModel);
                topic.update(regenbogenCategory);
                // 11) Create "Verwandte und Angehörige"
                TopicModel verwandte = createFamportalCategoryTopicModel("Verwandte und Angeh\u00F6rige", 140,
                        "category-7ba8c969-81ea-43c1-9e2d-7457ea4ca82f-de_DE-1");
                ChildTopicsModel verwandteModel = new ChildTopicsModel();
                verwandteModel.add("famportal.category", verwandte);
                TopicModel verwandteCategory = new TopicModel("famportal.category", verwandteModel);
                topic.update(verwandteCategory);
                // 12) "Menschen mit Behinderung"
                TopicModel menschenMit = createFamportalCategoryTopicModel("Menschen mit Behinderung", 150,
                        "category-c62e83e6-3cd5-4a77-a2f4-2be42ea59ee7-de_DE-1");
                ChildTopicsModel menschenMitModel = new ChildTopicsModel();
                menschenMitModel.add("famportal.category", menschenMit);
                TopicModel menschenMitCategory = new TopicModel("famportal.category", menschenMitModel);
                topic.update(menschenMitCategory);
                // 13) "Menschen und Sucht"
                TopicModel menschenSucht = createFamportalCategoryTopicModel("Menschen und Sucht", 160,
                        "category-4a469160-36de-4a6f-bc0a-4920de9e3802-de_DE-1");
                ChildTopicsModel menschenSuchtModel = new ChildTopicsModel();
                menschenSuchtModel.add("famportal.category", menschenSucht);
                TopicModel menschenSuchtCategory = new TopicModel("famportal.category", menschenSuchtModel);
                topic.update(menschenSuchtCategory);
                // 15) "Ehrenamtliche und Freiwillige"
                TopicModel ehrenamtliche = createFamportalCategoryTopicModel("Ehrenamtliche " +
                        "und Freiwillige", 170, "category-05abd661-880e-4dce-89d7-99900b065e65-de_DE-1");
                ChildTopicsModel ehrenamtlicheModel = new ChildTopicsModel();
                ehrenamtlicheModel.add("famportal.category", ehrenamtliche);
                TopicModel ehrenamtlicheCategory = new TopicModel("famportal.category", ehrenamtlicheModel);
                topic.update(ehrenamtlicheCategory);
                // 16) "Unternehmen"
                TopicModel unternehmen = createFamportalCategoryTopicModel("Unternehmen", 180,
                        "category-dc42cede-eafd-47eb-83f0-b12975955333-de_DE-1");
                ChildTopicsModel unternehmenModel = new ChildTopicsModel();
                unternehmenModel.add("famportal.category", unternehmen);
                TopicModel unternehmenCategory = new TopicModel("famportal.category", unternehmenModel);
                topic.update(unternehmenCategory);
                // 17) "Verbnde, Institutionen, Netzwerke"
                TopicModel vin = createFamportalCategoryTopicModel("Verb\u00e4nde, Institutionen, Netzwerke", 210,
                        "category-7fc0b896-b194-4222-9421-5dd6cc7eb02f-de_DE-1");
                ChildTopicsModel vinModel = new ChildTopicsModel();
                vinModel.add("famportal.category", vin);
                TopicModel vinCategory = new TopicModel("famportal.category", vinModel);
                topic.update(vinCategory);
            } else if (topic.getUri().equals("famportal.category-7f4ac210-a92c-4722-ae40-e7a7b8b456d6-de_DE-1")) {
                // Rename to "Schulkinder"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Schulkinder"));
                // Remove reference at "Kinder und Jugendliche"
                removeParentalReference(topic, "famportal.category-0e103816-0320-4c22-8be9-3af0544299c0-de_DE-1");
                // Move to "Familie+"
                assignTopicNewParent(topic, familiePlus);
            } else if (topic.getUri().equals("famportal.category-5dc53f71-20c8-47c4-bab7-1fbeb51d42d4-de_DE-1")) {
                // Rename to "Jugendliche"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Jugendliche"));
                // Remove reference at "Kinder und Jugendliche"
                removeParentalReference(topic, "famportal.category-0e103816-0320-4c22-8be9-3af0544299c0-de_DE-1");
                // Move to "FAmilie+"
                assignTopicNewParent(topic, familiePlus);
            } else if (topic.getUri().equals("famportal.category-3c66fa54-1edb-4378-aa11-000b92f095c8-de_DE-1")) {
                // Rename to "Eltern"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Eltern"));
            } else if (topic.getUri().equals("famportal.category-854b49b9-e9cb-4b08-a2c3-32abf8acc572-de_DE-1")) {
                // Rename to "Menschen mit Krankheit"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Menschen mit Krankheit"));
                // Remove reference at "Gesundheit"
                removeParentalReference(topic, "famportal.category-eaf6576d-c2c7-4828-bb0a-4dc0ecd90c83-de_DE-1");
                // Add reference to "Familie+"
                assignTopicNewParent(topic, familiePlus);
            } else if (topic.getUri().equals("famportal.category-6ad83bfa-3afd-46f6-b3cd-b82825d8387a-de_DE-1")) {
                // Rename to "Migranten und Flüchtlinge"
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Migranten und " +
                        "Fl\u00fcchtlinge"));
                // Remove reference at "Eltern und Erwachsene"
                removeParentalReference(topic, "famportal.category-3c66fa54-1edb-4378-aa11-000b92f095c8-de_DE-1");
                // Add new reference to "Familie+"
                assignTopicNewParent(topic, familiePlus);
            } else if (topic.getUri().equals("famportal.category-13c11ad9-0df4-41bf-9f79-5d2fc149d095-de_DE-1")) {
                // Remove "Pflegebedürftige" reference at "Gesundheit"
                removeParentalReference(topic, "famportal.category-5560b21e-d319-482d-a92a-0bcfb5447d5e-de_DE-1");
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Pflegebed\u00fcrftige"));
                // Add new reference to "Familie+"
                assignTopicNewParent(topic, familiePlus);
            }

            // ...
            /** } else if (topic.getUri().equals("famportal.category-ce00f1b7-7098-4e0a-9a6d-c4b3ea048903-de_DE-1")) {
             // todo: Ehe -> 5
             } else if (topic.getUri().equals("famportal.category-7f848dc3-bbb2-4fb3-aca1-514aee6979c9-de_DE-1")) {
             // todo: Eheähnliche Gemeinschaft -> 6
             } else if (topic.getUri().equals("famportal.category-90e572f8-f284-40b7-859f-a6097a160e75-de_DE-1")) {
             // todo: Lebenspartnerschaft -> 7 **/

        }
    }

    private void assignTopicNewParent(Topic element, Topic parent) {
        ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
        categoryTopicModel.addRef("famportal.category", element.getId());
        TopicModel category = new TopicModel("famportal.category", categoryTopicModel);
        parent.update(category);
    }

    private void removeParentalReference(Topic element, String famportalCategoryTopicURI) {
        Topic parent = dms.getTopic("uri", new SimpleValue(famportalCategoryTopicURI));
        ChildTopicsModel updateModel = new ChildTopicsModel();
        updateModel.addDeletionRef("famportal.category", element.getId());
        TopicModel category = new TopicModel("famportal.category", updateModel);
        parent.update(category);
    }

    private TopicModel createFamportalCategoryTopicModel(String name, int order, String uriPart) {
        // ..) Create new child topics
        ChildTopicsModel categoryChilds = new ChildTopicsModel();
        categoryChilds.put("famportal.category.name", name);
        categoryChilds.put("famportal.category.order", order);
        // ..) Create model for new composite topic
        return new TopicModel("famportal." + uriPart, "famportal.category", categoryChilds);
    }

}
