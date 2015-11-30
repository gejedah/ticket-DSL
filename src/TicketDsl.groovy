import groovy.xml.MarkupBuilder
/**
 * Processes a simple DSL to create various formats of a ticket: html
 */
class TicketDsl {
    private static HashMap<String, Boolean> invocation_effect = new HashMap()
    private static HashMap<String, List<Attr>> sections = new HashMap<>()
    private static Vector<String> obligeAttrs = new Vector<>()

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
            obligeAttrs.add(methodName)
//            println "argumen pertama" + obligeAttrs.get(methodName).get(0)
        }
        else{
            ArrayList tes = new ArrayList<>()
            tes = args
            tes.removeAll(Collections.singleton("")) // remove ""
            def Attr = new Attr(name: methodName, vals: tes)
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
                    int idx = 0
                    div(class: "container"){
                        div(class: "logo"){
                            img(src: "logo.png", alt: "Logo", align: "middle")
                            h1(class: "brand required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                            idx++
                            p(class: "tagline required", id: "${obligeAttrs.get(idx)}"){
                                strong("\$".concat(obligeAttrs.get(idx)))
                                idx++
                            }
                        }
                        div(class: "clear")
                        div(class: "booking details"){
                            h3(class: "booking-details-label", "Booking Details")
                            table(width: "100%"){
                                tr(){
                                    td("Agent Name")
                                    td(class: "required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td("Issued Date")
                                    td(class: "required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                                    idx++
                                }
                                tr(){
                                    td("Booking Reference")
                                    td{
                                        strong(class: "required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                                        idx++
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
                                    tr(){
                                        td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$passenger.".concat(obligeAttrs.get(idx)))
                                        idx++
                                        td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$passenger.".concat(obligeAttrs.get(idx)))
                                        idx++
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
                                tr(){
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                    td(class: "looping-required", id: "${obligeAttrs.get(idx)}", "\$itinerary.".concat(obligeAttrs.get(idx)))
                                    idx++
                                }
                            }
                        }
                        div(class: "payment-details"){
                            h3(class: "payment-details-label", "Payment Details")
                            table(width: "100%"){
                                tr(){
                                    td(class: "payment-table-left-column", "Nett Fare")
                                    td(class: "payment-table-right-column required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                                    idx++
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Taxes")
                                    td(class: "payment-table-right-column required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                                    idx++
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Total")
                                    td(class: "payment-table-right-column required", id: "total")
                                }
                            }
                        }
                        div(class: "notice"){
                            h3(class: "notice-label", "Notice")
                            p(class: "required", id: "${obligeAttrs.get(idx)}", "\$".concat(obligeAttrs.get(idx)))
                            idx++
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

