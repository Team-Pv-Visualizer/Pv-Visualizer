package at.htl.restclient.controller.School;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.htl.restclient.Common.JsonReader;
import at.htl.restclient.entities.School.Head;
import at.htl.restclient.genericoperations.CRUDOperations;
import at.htl.restclient.service.School.HeadService;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/Head")
@Produces(MediaType.APPLICATION_JSON)
public class HeadController {

    @Inject
    @RestClient
    HeadService headService;

    @Inject
    CRUDOperations crud;

    @Inject
    EntityManager em;

    /**
     * Json-Variable | To choose, what Service the user wants (Fornius, School)
     */
    private String service;

    static Integer counter = 0;

    /**
     * Call up the method data every 60sec
     */
    @Scheduled(every="60s")
    public void callUpMethod(){
        service = JsonReader.loadSettings();
        if(!service.contains("Fronius")){
            data();
            System.out.println("Call up Head API");
        }
    }

    /**
     * Call up Service and get Json File and process new data
     * @return response code and new Head Json
     */
    @GET
    public Response data() {
        Head head = new Head();
        try {
            head = headService.getAll();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(counter == 0){
            crud.add(head);
            counter++;
        }
        else{
            if(controlColumn(head.timeStamp) != true){
                crud.add(head);
            }
        }

        return Response.ok(head).build();
    }

    /**
     * Compare two timeStamps if the newer one exist already
     * @param timestamp new timeStamp
     * @return Boolean value if the newer one is already created
     */
    @Transactional
    private Boolean controlColumn(String timestamp){
        Boolean duplicate = false;
        em.createQuery("select x from Head x where x.timeStamp = :timestamp", Head.class).setParameter("timestamp", timestamp).getSingleResult();
        if(em != null){
            duplicate = true;
        }
        return duplicate;
    }

}