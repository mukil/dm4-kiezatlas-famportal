package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.Migration;

import java.util.logging.Logger;



public class Migration5 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        // 0) Categories to re-assign/move from one category to another
        Topic bundFuersLebenCategory = dms.getTopic("uri", new SimpleValue("famportal.category-68235a12-f151-494c-8e5d-c2dd5492449a-de_DE-1"));
        Topic notlagenCategory = dms.getTopic("uri", new SimpleValue("famportal.category-6f01c6f8-ccc7-4e37-a751-34590de758de-de_DE-1"));
        Topic elternWerdenCategory = dms.getTopic("uri", new SimpleValue("famportal.category-77726c72-c8eb-46ca-871e-0ef30e7e951e-de_DE-1"));
        Topic elternSeinCategory = dms.getTopic("uri", new SimpleValue("famportal.category-039e72df-4f50-4f0c-b744-caf52d18fc77-de_DE-1"));
        Topic verwandschaftCategory = dms.getTopic("uri", new SimpleValue("famportal.category-7a33ad19-5075-4f6d-8589-f03e996fccba-de_DE-1"));

        // 1) Insert new categories into the famportal category tree
        for (Topic topic : dms.getTopics("famportal.category", 0).getItems()) {

            // 1.1) Neue Kategorie "Spielpaetze" in "Freizeit und Kultur" einfuegen
            if (topic.getUri().equals("famportal." + "category-973782a5-e91f-44af-b313-22f6c736d4c2-de_DE-1")) {
                topic.loadChildTopics();
                // ..) Create new child topics
                ChildTopicsModel categoryChilds = new ChildTopicsModel();
                categoryChilds.put("famportal.category.name", "Spielpl\u00e4tze");
                categoryChilds.put("famportal.category.order", 70);
                // ..) Create model for new composite topic
                TopicModel childsTopicModel = new TopicModel("famportal.category-cd5e8503-fa57-4a38-bc8e-0ab32bfd8e7e-de_DE-1",
                    "famportal.category", categoryChilds);
                // ..) Add new composite topic as child topic to our current topic
                ChildTopicsModel categoryTopicModel = new ChildTopicsModel();
                categoryTopicModel.add("famportal.category", new TopicModel(childsTopicModel));
                // ..) Update our current topic with appending our new composite child topic
                TopicModel category = new TopicModel(null, categoryTopicModel);
                topic.update(category);

            // 1.2) Neue URI fuer Famportal Category "Eltern werden"
            } else if (topic.getUri().equals("famportal.category-b7b85cb4-5695-4e8c-8924-7fa53ed5c9ef-de_DE-1")) {
                topic.setUri("famportal.category-a9be22b3-3fb6-48e5-99a6-2814c6b0f237-de_DE-1");

            // 1.3) Fix typo in name of "Bund fuers Leben eingehen"
            } else if (topic.getUri().equals("famportal.category-8c24a931-4844-433e-96f6-673d0def3924-de_DE-1")) {
                topic.setChildTopics(new ChildTopicsModel().put("famportal.category.name", "Bund f\u00fcrs Leben eingehen"));

            // 1.4) Remove \"Bund fürs Leben\" and \"Notlagen\" tree from \"Themen\"
            } else if (topic.getUri().equals("famportal.category-0f89fb19-e9d8-4aeb-86bf-d78b1b41d17f-de_DE-1")) {
                logger.info("Fetched \""+topic.getSimpleValue()+"\" famportal-category to delete refs to childs "
                    + bundFuersLebenCategory.getSimpleValue() + " and " + notlagenCategory.getSimpleValue());
                ChildTopicsModel deleteRef = new ChildTopicsModel();
                deleteRef.addDeletionRef("famportal.category", bundFuersLebenCategory.getId());
                deleteRef.addDeletionRef("famportal.category", notlagenCategory.getId());
                topic.update(new TopicModel("famportal.category", deleteRef));

            // 1.5) Add \"Bund fürs Leben\" tree to \"Familie + Partnerschaft\"
            } else if (topic.getUri().equals("famportal.category-e99c03aa-a0cb-44e4-a518-3ab4b7062679-de_DE-1")) {
                ChildTopicsModel addChildRef = new ChildTopicsModel();
                addChildRef.addRef("famportal.category", bundFuersLebenCategory.getId());
                topic.update(new TopicModel("famportal.category", addChildRef));
                logger.info("Moved \"Bund fürs Leben\" famportal-category to new parent " + topic.getSimpleValue());

            // 1.6) Add \"Notlagen\" tree to \"Beratung + Unterstützung\"
            } else if (topic.getUri().equals("famportal.category-a0e061b5-83b0-4c64-818c-a53c3137c2c9-de_DE-1")) {
                ChildTopicsModel addChildRef = new ChildTopicsModel();
                addChildRef.addRef("famportal.category", notlagenCategory.getId());
                topic.update(new TopicModel("famportal.category", addChildRef));
                logger.info("Moved \"Notlagen\" famportal-category to new parent " + topic.getSimpleValue());

            // 1.7) Remove three refs from "Lebenslagen"
            } else if (topic.getUri().equals("famportal.category-bb19be3d-f705-4516-83f3-517ca277a73f-de_DE-1")) {
                logger.info("Fetched \""+topic.getSimpleValue()+"\" famportal-category to delete refs to childs "
                    + elternWerdenCategory.getSimpleValue() + ", " + verwandschaftCategory + " and " + elternSeinCategory.getSimpleValue());
                ChildTopicsModel deleteRef = new ChildTopicsModel();
                deleteRef.addDeletionRef("famportal.category", elternWerdenCategory.getId());
                deleteRef.addDeletionRef("famportal.category", elternSeinCategory.getId());
                deleteRef.addDeletionRef("famportal.category", verwandschaftCategory.getId());
                topic.update(new TopicModel("famportal.category", deleteRef));

            // 1.8) Add three refs to "Familie sein"
            } else if (topic.getUri().equals("famportal.category-99bc3d7e-9d0b-43f2-b9ed-960b24c85cef-de_DE-1")) {
                ChildTopicsModel addChildRef = new ChildTopicsModel();
                addChildRef.addRef("famportal.category", elternWerdenCategory.getId());
                addChildRef.addRef("famportal.category", elternSeinCategory.getId());
                addChildRef.addRef("famportal.category", verwandschaftCategory.getId());
                topic.update(new TopicModel("famportal.category", addChildRef));
            }

        }

    }
    
}
