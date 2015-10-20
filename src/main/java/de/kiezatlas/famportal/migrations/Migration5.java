package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.Migration;

import java.util.logging.Logger;



public class Migration5 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
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
			}
		}
	}
    
}
