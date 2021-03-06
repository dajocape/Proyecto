package com.resources;

import com.dao.AsientoDAO;
import com.models.Asiento;
import com.models.Lugar;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("asiento")
public class AsientoResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public static List<Asiento> getAsientos() {
        AsientoDAO dao = new AsientoDAO();
        List<Asiento> listaAsientos = dao.getAsientos();
        return listaAsientos;
    }
    
    @GET
    @Path("/{idAsiento}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Asiento getAsiento(@PathParam("idAsiento") int idAsiento) {
        Asiento asiento;
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://127.0.0.1:6379");
        
        RedissonClient redisson = Redisson.create(config);
        RBucket<Asiento> bucket = redisson.getBucket("asiento_" + Integer.toString(idAsiento));
        asiento = bucket.get();
        if(asiento == null){
            AsientoDAO dao = new AsientoDAO();
            asiento = dao.getAsiento(idAsiento);
            bucket.set(asiento);
        }
        
        redisson.shutdown();
        return asiento;
    }
    
    @POST    
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Asiento addAsiento(@FormParam("categoria") String categoria, 
            @FormParam("numero") int numero, @FormParam("lugar") int lugar) {
        AsientoDAO dao = new AsientoDAO();
        Lugar lug = LugarResource.getLugar(lugar);
        Asiento asiento = new Asiento(categoria, numero, lug);
        Asiento asientoNuevo = dao.addAsiento(asiento);
        return asientoNuevo;
    }
    
    @PUT
    @Path("/update/{idAsiento}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response updateAsiento(@PathParam("idAsiento") int idAsiento, Asiento nuevoAsiento) {
        AsientoDAO dao = new AsientoDAO();
        int count = dao.updateAsiento(idAsiento, nuevoAsiento);
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://127.0.0.1:6379");
        
        RedissonClient redisson = Redisson.create(config);
        RBucket<Asiento> bucket = redisson.getBucket("asiento_" + Integer.toString(idAsiento));
        Asiento asiento = bucket.get();
        if(asiento != null){
            bucket.set(nuevoAsiento);
        }
        if(count==0){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }
 
    @DELETE
    @Path("/delete/{idAsiento}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response deleteAsiento(@PathParam("idAsiento") int idAsiento) {
        AsientoDAO dao = new AsientoDAO();
        int count = dao.deleteAsiento(idAsiento);
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://127.0.0.1:6379");
        
        RedissonClient redisson = Redisson.create(config);
        RBucket<Asiento> bucket = redisson.getBucket("asiento_" + Integer.toString(idAsiento));
        Asiento asiento = bucket.get();
        if(asiento != null){
            bucket.delete();
        }
        if(count==0){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }
}