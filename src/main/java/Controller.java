import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
/**
 * public Controller classed, used to fetch Data from localhost, calculate the time used by any single user
 * and post that time together with their respective customerId back to localhost
 * */
public class Controller {
    private URL url;

    /*
    * public getDataset8080 function, takes the localhost URL, checks the status code to see whether the access was successful
    * and generates a String based on the fetched infos, which are then converted into a Dataset object for later use
    * */
    public Dataset getDataset8080(){
        try{
            HttpClient httpClient = HttpClient.newHttpClient();
            url = new URL("http://localhost:8080/v1/dataset");
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(url.toURI()).GET().build();
            HttpResponse<String> clientResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if(clientResponse.statusCode()==200){
                String response = clientResponse.body();
                Gson gson = new Gson();
                return gson.fromJson(response, Dataset.class);
            }else{
                System.out.println("Error retrieving Dataset, wrong Status Code: "+clientResponse.statusCode());
                return null;
            }

        }catch(MalformedURLException m){
            m.printStackTrace();
            System.out.println("URL not correct");
            return null;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    * public getResult method
    * goes through the Event list 1 by 1 to find matching customer IDs and eventtypes in order to calculate difference between the 2
    * returns a Result Object with a list of customers
    * @param dataset previously fetched Dataset
    * @returns result calculated result object containing a List of all customers with both start and stop timestamp
    * */
    public Result getResult(Dataset dataset){
        List<Customer> list2 = null;
        if(dataset!=null){
            List<Event> list = dataset.events;
            for(Event e:list){
                for(Event j:list){
                    if(e.customerId==j.customerId){
                        if((e.eventType.equals("start")&&j.eventType.equals("stop"))||(j.eventType.equals("start")&&e.eventType.equals("stop"))){
                                list2.add(new Customer(e.customerId, Math.abs(e.timestamp-j.timestamp)));
                        }
                    }
                }
            }
        }
        Result result= new Result();
        result.setCustomers(list2);
        return result;
    }

    /*
    * public sendResult function, takes a Json formatted String and posts it to the localhost URL
    * @param result previously fetched and calculated result String
    * @returns null
    * */
    public void sendResult(String result){
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            url = new URL("http://localhost:8080/v1/result");
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(url.toURI())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(result))
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public class Result{
        List<Customer> customers;

        public List<Customer> getCustomers() {
            return customers;
        }
        public void setCustomers(List<Customer> customers) {
            this.customers = customers;
        }
        @Override public String toString(){
            return "{\n"+
                    "result: [\n"+
                    customers+
                    "]\n"+
                    "}";
        }
    }
    public record Customer(String customerId, float consumption){
        @Override public String toString(){
            return "{\n"+
                    "customerId: "+customerId+",\n"+
                    "consumption: "+consumption+"\n"+
                    "}\n";

        }
    }

    public record Event(String customerId, String workloadId, float timestamp, String eventType){
        @Override public String toString(){
            return "{\n"+"customerId: "+ customerId+",\n"+
                    "workloadId: "+ workloadId +",\n"+
                    "timestamp: "+ timestamp +",\n"+
                    "eventType: "+ eventType +",\n"+
                    "}\n";
        }
    }
    public record Dataset(List<Event> events){
        @Override public String toString(){
            return "DataSet\n"+
                    "{\n"+
                    "   events: [\n"+
                        events+
                    "]\n"+
                    "}";

        }
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        Dataset dataset = controller.getDataset8080();
        Gson gson = new Gson();
        String result = gson.toJson(controller.getResult(dataset));
        controller.sendResult(result);
    }
}
