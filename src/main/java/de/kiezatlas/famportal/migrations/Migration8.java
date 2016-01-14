package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.plugins.workspaces.WorkspacesService;

import java.util.logging.Logger;


public class Migration8 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger log = Logger.getLogger(getClass().getName());


    @Inject private WorkspacesService workspaceService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        // -1) "Wiedereinstieg in den Beruf" eine Ebene höher hängen
        /** Topic ausbildungUndBeruf = dms.getTopic("uri", new SimpleValue("famportal" +
         ".category-20186c9c-a413-421f-8f1e-a80fb266d79f-de_DE-1"));
         Topic wiederEinstieg = dms.getTopic("uri", new SimpleValue("famportal" +
         ".category-2d179e10-5e95-409e-8f65-fe492b6422cf-de_DE-1"));
         ChildTopicsModel existingModel = ausbildungUndBeruf.getChildTopics().getModel();
         existingModel.addRef("famportal.category", wiederEinstieg.getId());
         ausbildungUndBeruf.update(new TopicModel(existingModel));
         Topic vereinbarkeit = dms.getTopic("uri", new SimpleValue("famportal" +
         ".category-b67338c6-9346-47b1-9fed-36e33a6b698d-de_DE-1"));
         ChildTopicsModel anotherModel = ausbildungUndBeruf.getChildTopics().getModel();
         existingModel.addDeletionRef("famportal.category", wiederEinstieg.getId());
         vereinbarkeit.update(new TopicModel(anotherModel)); */

        // 0) Delet "nach der schule"
        Topic nachDerSchule = dms.getTopic("uri", new SimpleValue("famportal.category-b5cbe927-3a68-4e82-b916-23e913321162-de_DE-1"));
        log.info("## Deleting Famportal Category Topic " + nachDerSchule.getSimpleValue());
        nachDerSchule.delete();

        // 1) delete "Familie sein"
        Topic familieSein = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-99bc3d7e-9d0b-43f2-b9ed-960b24c85cef-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + familieSein.getSimpleValue());
        familieSein.delete();

        // 2) delete "Eltern werden"
        Topic elternWerden = dms.getTopic("uri", new SimpleValue("famportal.category-77726c72-c8eb-46ca-871e-0ef30e7e951e-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + elternWerden.getSimpleValue());
        elternWerden.delete();

        // 3) delete "Eltern sein"
        Topic elternSein = dms.getTopic("uri", new SimpleValue("famportal.category-039e72df-4f50-4f0c-b744-caf52d18fc77-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + elternSein.getSimpleValue());
        elternSein.delete();

        // 4) delete "Verwandschaft"
        Topic verwandschaft = dms.getTopic("uri", new SimpleValue("famportal.category-7a33ad19-5075-4f6d-8589-f03e996fccba-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + verwandschaft.getSimpleValue());
        verwandschaft.delete();

        // 5) delete "Bund fürs Leben"
        Topic bundFuersLeben = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-8c24a931-4844-433e-96f6-673d0def3924-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + bundFuersLeben.getSimpleValue());
        bundFuersLeben.delete();

        // 6) Delete "Wohnen"
        Topic wohnen = dms.getTopic("uri", new SimpleValue("famportal.category-2d843bbd-25eb-4c15-b69b-bd85c1765d00-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + wohnen.getSimpleValue());
        wohnen.delete();

        // 7) Delete "Ruhestand"
        Topic ruhestand = dms.getTopic("uri", new SimpleValue("famportal.category-04e58d69-f48b-4847-b60f-9b3c4b0861f8-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + ruhestand.getSimpleValue());
        ruhestand.delete();

        // 8) Delete "Unterstützung im Alltag"
        Topic unterstuetzungImAlltag = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-74fbaa2c-8b79-40b6-b262-cbf4735c6f01-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + unterstuetzungImAlltag.getSimpleValue());
        unterstuetzungImAlltag.delete();

        // 9) Delete "Leben mit Kindern"
        Topic lebenMitKindern = dms.getTopic("uri", new SimpleValue("famportal.category-62d6d43c-a80e-4a89-8eb6-fb4d88bbaf80-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + lebenMitKindern.getSimpleValue());
        lebenMitKindern.delete();

        // 10) Delete "Bund fürs Leben"
        Topic bundFLeben = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-68235a12-f151-494c-8e5d-c2dd5492449a-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + bundFLeben.getSimpleValue());
        bundFLeben.delete();

        // 11) Delete "Notlagen"
        Topic notlagen = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-6f01c6f8-ccc7-4e37-a751-34590de758de-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + notlagen.getSimpleValue());
        notlagen.delete();

        // 12) Delete "Gemeinsame Deklaration"
        Topic deklaration = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-b9edc61c-b224-4323-adb6-455bd01ba40f-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + deklaration.getSimpleValue());
        deklaration.delete();

        // 13) Delete "Schulformen"
        Topic schulformen = dms.getTopic("uri", new SimpleValue("famportal.category-ba9ac47b-b073-4791-a249-8b1cfee5f5de-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + schulformen.getSimpleValue());
        schulformen.delete();

        // 14) Delete "Kinderbetreuung"
        Topic kinderbetreuung = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-66af2938-d7aa-4013-8c8a-5af6ff31be9f-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + kinderbetreuung.getSimpleValue());
        kinderbetreuung.delete();

        // 15) Delete "Gefordert sein"
        Topic gefordertSein = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-1c1a2b86-eb0f-4fe2-85ba-5dc2ba93c100-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + gefordertSein.getSimpleValue());
        gefordertSein.delete();

        // 16) Delete "Pflege"
        Topic pflegeLeben = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-72e76ca1-f558-4855-acc6-a64b8f0c6110-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + pflegeLeben.getSimpleValue());
        pflegeLeben.delete();

        // 17) Delete "Gepflegt werden"
        Topic gepflegtWerden = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-6aa2cb58-bd5f-484d-b90c-1ad25aa7bcb7-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + gepflegtWerden.getSimpleValue());
        gepflegtWerden.delete();

        // 18) Delete "Lernen und Arbeiten"
        Topic lernenUndArbeiten = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-7d827b65-b126-4174-bcdc-ee4853896830-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + lernenUndArbeiten.getSimpleValue());
        lernenUndArbeiten.delete();

        // 19) Delete "Für sich und andere sorgen"
        Topic fuerSichUndAndere = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-af840d52-ab75-4b95-9779-216b1ccb01d6-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + fuerSichUndAndere.getSimpleValue());
        fuerSichUndAndere.delete();

        // 20) Delete "Für sich und andere sorgen"
        Topic erbenUndVererben = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-7b0a26aa-9338-4d2c-a50c-99607b0eb059-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + erbenUndVererben.getSimpleValue());
        erbenUndVererben.delete();

        // 21) Delete "Schulformen 2"
        Topic schulFormen = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-e0ea1edd-f0ba-4bc3-bb7d-2ff47c69e73d-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + schulFormen.getSimpleValue());
        schulFormen.delete();

        // 22) Delete "Schulformen 3"
        Topic schulFormen3 = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-7c5596d9-f408-41d6-abb6-f127d761b7fb-de_DE-1"));
        log.info("### Deleting Famportal Category Topic " + schulFormen3.getSimpleValue());
        schulFormen3.delete();

        // 23) Delete all sub-categories of "Eltern
        Topic eltern = dms.getTopic("uri", new SimpleValue("famportal.category-3c66fa54-1edb-4378-aa11-000b92f095c8-de_DE-1"));
        deleteAllSubcategories(eltern);

        // 24) Delete category including all sub-categories from "Kinder und Jugendliche"
        Topic kuj = dms.getTopic("uri", new SimpleValue("famportal.category-0e103816-0320-4c22-8be9-3af0544299c0-de_DE-1"));
        deleteAllSubcategories(kuj);
        // Including Parent
        kuj.delete();

        // 25) Delete category including all sub-categories from "Großeltern und ältere Menschen"
        Topic guame = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-40c63dee-628a-429f-a039-6282f63da459-de_DE-1"));
        deleteAllSubcategories(guame);

        // 26) Delete category including all sub-categories from "Freunde und Nachbarn"
        Topic fun = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-a668ba08-74fc-4722-a13e-e90c50755df3-de_DE-1"));
        deleteAllSubcategories(fun);

        // 27) Delete category including all sub-categories from "Fachkräfte für Familien"
        Topic fff = dms.getTopic("uri", new SimpleValue("famportal" +
                ".category-c8bc4e13-1c82-4694-b82b-5702ab66cd76-de_DE-1"));
        deleteAllSubcategories(fff);
    }

    private void deleteAllSubcategories(Topic guame) {
        guame.loadChildTopics();
        if (guame.getChildTopics().has("famportal.category")) {
            for (Topic el : guame.getChildTopics().getTopics("famportal.category")) {
                el.loadChildTopics();
                if (el.getChildTopics().has("famportal.category")) {
                    for (Topic sub : el.getChildTopics().getTopics("famportal.category")) {
                        log.info("Deleting Sub-Subcategory from Großeltern und ältere Menschen\" " + sub.getSimpleValue());
                        sub.delete();
                    }
                }
                log.info("Deleting Subcategory from Großeltern und ältere Menschen\" " + el.getSimpleValue());
                el.delete();
            }
        }
    }

}
