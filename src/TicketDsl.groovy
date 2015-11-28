import groovy.xml.MarkupBuilder
/**
 * Processes a simple DSL to create various formats of a ticket: html
 */
class TicketDsl {

    String vcompanyName
    String vmotto
    String vagentName
    def optAttrs = []
    def obligeAttrs = []
    Vector<String> sections

    private static HashMap<String, Boolean> invocation_effect = new HashMap()

    public TicketDsl(){
        File file = new File("src/config.txt")
        List<String> oblig_atts = file.readLines()
        for (String att : oblig_atts){
//            println att
            invocation_effect.put(att, false)
        }
        sections = new Vector<>()
    }

    /**
     * When a method is not recognized, assume it is a title for a new section. Create a simple
     * object that contains the method name and the parameter which is the body.
     */
    def methodMissing(String methodName, args) {
//        println("Method Missing being invoked")
        def Attr = new Attr(name: methodName, vals: args)
        if (invocation_effect.containsKey(methodName)){
            obligeAttrs << Attr
            invocation_effect.replace(methodName, true)
        }
        else{
            optAttrs << Attr
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

                            .logo, .logo > img {
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
                            h1(class: "brand", "Tiger Air")
                            p(class: "tagline"){
                                strong("We make people fly")
                            }
                        }
                        div(class: "clear")
                        div(class: "booking details"){
                            h3(class: "booking-details-label", "Booking Details")
                            table(width: "100%"){
                                tr(){
                                    td("Agent Name")
                                    td("PT. Trinusa Travelindo")
                                    td("Issued Date")
                                    td("Tanggal")
                                }
                                tr(){
                                    td("Booking Reference")
                                    td{
                                        strong("NLAWB")
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
                                tr(){
                                    td("pet")
                                    td("72109212924124")
                                }
                                tr(){
                                    td("Mrs. Jeanice Ginting")
                                    td("97009249741123")
                                }
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
                                    td("21 APR 2016")
                                    td()
                                    td()
                                    td()
                                    td()
                                    td()
                                    td()
                                    td()
                                }
                            }
                        }
                        div(class: "payment-details"){
                            h3(class: "payment-details-label", "Payment Details")
                            table(width: "100%"){
                                tr(){
                                    td(class: "payment-table-left-column", "Nett Fare")
                                    td(class: "payment-table-right-column", "IDR 2500000")
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Taxes")
                                    td(class: "payment-table-right-column", "IDR 250000")
                                }
                                tr(){
                                    td(class: "payment-table-left-column", "Total")
                                    td(class: "payment-table-right-column", "IDR 2750000")
                                }
                            }
                        }
                        div(class: "notice"){
                            h3(class: "notice-label", "Notice")
                            p("Morbi leo risus, porta ac consectetur ac, vestibulum at eros. Aenean eu leo quam. Pellentesque ornare sem lacinia quam venenatis vestibulum. Maecenas sed diam eget risus varius blandit sit amet non magna. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec id elit non mi porta gravida at eget metus. Vestibulum id ligula porta felis euismod semper. Cras justo odio, dapibus ac facilisis in, egestas eget quam.")
                        }
                    }
                }
            }
            File file = new File("src/out.html")
            file.write(writer.toString())
//        println writer
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
            inputs = input.split("\\s+")
            if (input.length() > 1){
                param = Arrays.copyOfRange(inputs, 1, inputs.length)
//            println "byk elemen yg diinput: " + inputs.length
//            for (int i = 0; i < inputs.length; i++) {
//                println "Elemen ke $i adalah " + inputs[i]
//            }
            }
            ticketDsl.invokeMethod(inputs[0], param)
//            println "Masukkan input anda: "
        }

    }
}

