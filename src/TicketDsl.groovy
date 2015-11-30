import groovy.xml.MarkupBuilder
/**
 * Processes a simple DSL to create various formats of a ticket: html
 */
class TicketDsl {
    String vcompanyName
    String vmotto
    String vagentName

    private static HashMap<String, Boolean> invocation_effect = new HashMap()
    private static HashMap<String, List<Attr>> sections = new HashMap<>()
    private static HashMap<String, List<String>> obligeAttrs = new HashMap<>()

    public TicketDsl(){
        File file_attr = new File("src/config.txt")
        List<String> oblig_atts = file_attr.readLines()
        for (String att : oblig_atts){
//            println att
            invocation_effect.put(att, false)
        }

        File file_section = new File("src/config1.txt")
        List<String> tempList = file_section.readLines()
        for (String section: tempList){
            sections.put(section, new ArrayList<Attr>())
        }
    }

    /**
     * When a method is not recognized, assume it is a title for a new section. Create a simple
     * object that contains the method name and the parameter which is the body.
     */
    def methodMissing(String methodName, args) {
//        println("Method Missing being invoked")
        if (invocation_effect.containsKey(methodName)){
            invocation_effect.replace(methodName, true)
            ArrayList tes = new ArrayList<>()
            tes = args
            tes.removeAll(Collections.singleton("")) // remove ""
            obligeAttrs.put(methodName, tes)
//            println "argumen pertama" + obligeAttrs.get(methodName).get(0)
        }
        else{
            def Attr = new Attr(name: methodName, vals: args)
            if (args.length >= 1){
                if (sections.containsKey(args[0])){
                    sections.get(args[0]).add(Attr)
                }
                else{
                    sections.put(args[0], new ArrayList<Attr>().add(Attr))
                }
            }
            else{
                sections.get("etc").add(Attr)
            }
        }
    }

    /**
     * 'get' methods get called from the dsl by convention. Due to groovy closure delegation,
     * we had to place MarkUpBuilder and StringWrite code in a static method as the delegate of the closure
     * did not have access to the system.out
     */
    def getHtml() {
        println("Method getHtml being invoked")
        doHtml(this)
    }

    /**
     * Use markupBuilder to create an html output
     */
    private static doHtml(TicketDsl ticketDsl) {
        println "dohtml"
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        if (invocation_effect.containsValue(false)){
            println("Ada atribut yang belum didefinisikan!!")
        }
        else{
            xml.html() {
                head {
                    title("Ticket")
                    style('''body{
                        background:none;
                    }

                    .container {
                        width:960px;
                        margin:auto;
                    }

                            .logo, .logo img {
                        width:200px;
                    }
                    .logo{
                        margin:auto;
                    }
                    .brand {
                        text-align: center;
                        color:red;
                        margin:0;
                    }
                    .tagline {
                        text-align:center;
                        margin:0;
                    }
                    .clear{clear:both;}
                            .booking-details-label, .passenger-details-label, .itinerary-details-label, .payment-details-label, .notice-label{border-bottom: 1px solid;}
                            #passenger-details-table-header, #itinerary-details-table-header{text-align:left;}
                            .payment-table-right-column{text-align: right;}''')
                }
                body {
                    div(class: "container"){
                        div(class: "logo"){
                            img(src: "logo.png", alt: "Logo", align: "middle")
                            h1(class: "brand required", id: "companyName", {obligeAttrs.get("companyName").isEmpty() ? "" : obligeAttrs.get("companyName").get(0)}.call())
                            p(class: "tagline required", id: "motto"){
                                strong({obligeAttrs.get("motto").isEmpty() ? "" : obligeAttrs.get("motto").get(0)}.call())
                            }
                        }
                        div(class: "clear")
                        div(class: "booking details"){
                            h3(class: "booking-details-label", "Booking Details")
                            if (obligeAttrs.get("agentName").isEmpty()){}
                            else{
                                table(width: "100%"){
                                    tr(){
                                        td("Agent Name")
                                        td(class: "required", id: "agentName", {obligeAttrs.get("agentName").isEmpty() ? "" : obligeAttrs.get("agentName").get(0)}.call())
                                        td("Issued Date")
                                        td(class: "required", id: "issuedDate", {obligeAttrs.get("issuedDate").isEmpty() ? "" : obligeAttrs.get("issuedDate").get(0)}.call())
                                    }
                                    tr(){
                                        td("Booking Reference")
                                        td{
                                            strong(class: "required", id: "bookRef", {obligeAttrs.get("bookRef").isEmpty() ? "" : obligeAttrs.get("bookRef").get(0)}.call())
                                        }
                                    }
                                }
                            }
                        }
                        div(class: "passenger-details"){
                            h3(class: "passenger-details-label", "Passenger Details")
                            table(width: "100%"){
                                tr(id: "passenger-details-table-header"){
                                    th("Name")
                                    th("eTicket Number")
                                }
                                Closure tes = {
                                    int i = 0
                                    while (i < obligeAttrs.get("passengerName").size()){
                                        tr(){
                                            td(class: "required", id: "passengerName $i", {obligeAttrs.get("passengerName").isEmpty() ? "" : obligeAttrs.get("passengerName").get(i)}.call())
                                            td(class: "required", id: "eTicketNum $i", {obligeAttrs.get("eTicketNum").isEmpty() ? "" : obligeAttrs.get("eTicketNum").get(i)}.call())
                                        }
                                        i++
                                    }
                                }
                                tes.call()
                            }
                        }
                        div(class: "itinerary-details"){
                            h3(class: "itinerary-details-label", "Itenarary Details")
                            table(width: "100%"){
                                tr(id: "itinerary-details-table-header"){
                                    th("Date")
                                    th("Flight")
                                    th("Depart Airport")
                                    th("Arrive Airport")
                                    th("Depart Time")
                                    th("Arrive Time")
                                    th("Class")
                                    th("Bagg.")
                                }
                                Closure tes = {
                                    int i = 0
                                    while (i<obligeAttrs.get("iteneraryDate").size()){
                                        tr(){
                                            td(class: "required", id: "iteneraryDate $i", {obligeAttrs.get("iteneraryDate").isEmpty() ? "" : obligeAttrs.get("iteneraryDate").get(i)}.call())
                                            td(class: "required", id: "iteneraryFlight $i", {obligeAttrs.get("iteneraryFlight").isEmpty() ? "" : obligeAttrs.get("iteneraryFlight").get(i)}.call())
                                            td(class: "required", id: "iteneraryDepartAirport $i", {obligeAttrs.get("iteneraryDepartAirport").isEmpty() ? "" : obligeAttrs.get("iteneraryDepartAirport").get(i)}.call())
                                            td(class: "required", id: "iteneraryArriveAirport $i", {obligeAttrs.get("iteneraryArriveAirport").isEmpty() ? "" : obligeAttrs.get("iteneraryArriveAirport").get(i)}.call())
                                            td(class: "required", id: "iteneraryDepartTime $i", {obligeAttrs.get("iteneraryDepartTime").isEmpty() ? "" : obligeAttrs.get("iteneraryDepartTime").get(i)}.call())
                                            td(class: "required", id: "iteneraryArrivetime $i", {obligeAttrs.get("iteneraryArrivetime").isEmpty() ? "" : obligeAttrs.get("iteneraryArrivetime").get(i)}.call())
                                            td(class: "required", id: "iteneraryClass $i", {obligeAttrs.get("iteneraryClass").isEmpty() ? "" : obligeAttrs.get("iteneraryClass").get(i)}.call())
                                            td(class: "required", id: "iteneraryBaggage $i", {obligeAttrs.get("iteneraryBaggage").isEmpty() ? "" : obligeAttrs.get("iteneraryBaggage").get(i)}.call())
                                        }
                                        i++
                                    }
                                }
                                tes.call()
                            }
                        }
                        div(class: "payment-details"){
                            h3(class: "payment-details-label", "Payment Details")
                            table(width: "100%"){
                                tr(){
                                    td(class: "payment-table-left-column", "Nett Fare")
                                    td(class: "payment-table-right-column required", id: "paymentNett", {obligeAttrs.get("paymentNett").isEmpty() ? "" : obligeAttrs.get("paymentNett").get(0)}.call())
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Taxes")
                                    td(class: "payment-table-right-column required", id: "paymentTaxes", {obligeAttrs.get("paymentTaxes").isEmpty() ? "" : obligeAttrs.get("paymentTaxes").get(0)}.call())
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Total")
                                    td(class: "payment-table-right-column required", id: "total")
                                }
                            }
                        }
                        div(class: "notice"){
                            h3(class: "notice-label", "Notice")
                            p(class: "required", id: "notice", {obligeAttrs.get("notice").isEmpty() ? "" : obligeAttrs.get("notice").get(0)}.call())
                        }
                    }
                }
            }
            File file = new File("src/out.html")
            file.write(writer.toString())
        println "writer"
        }
    }

    public static void main(String[] args){
        println "Masukkan input anda: "

        BufferedReader bfReader = new BufferedReader(new InputStreamReader(System.in));

        TicketDsl ticketDsl = new TicketDsl()
        String input;
        String[] inputs
        String[] param

        while ((input = bfReader.readLine()) != null && input.length()!= 0 && input != "exit") {
            inputs = input.split("[<>]")
            if (input.length() > 1){
                param = Arrays.copyOfRange(inputs, 1, inputs.length)
                println "byk elemen yg diinput: " + inputs.length
                for (int i = 0; i < inputs.length; i++) {
                    println "Elemen ke $i adalah " + inputs[i]
                }
            }
            ticketDsl.invokeMethod(inputs[0], param)
            println "Finished "
        }

    }
}

