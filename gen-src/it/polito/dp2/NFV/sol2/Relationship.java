//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.3.1-b171012.0423 
// Vedere <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.09.17 alle 10:48:04 AM CEST 
//


package it.polito.dp2.NFV.sol2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per anonymous complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="dstNode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="srcNode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "relationship")
public class Relationship {

    @XmlAttribute(name = "dstNode", required = true)
    protected String dstNode;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "srcNode")
    protected String srcNode;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Recupera il valore della proprietà dstNode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDstNode() {
        return dstNode;
    }

    /**
     * Imposta il valore della proprietà dstNode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDstNode(String value) {
        this.dstNode = value;
    }

    /**
     * Recupera il valore della proprietà type.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Imposta il valore della proprietà type.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Recupera il valore della proprietà srcNode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcNode() {
        return srcNode;
    }

    /**
     * Imposta il valore della proprietà srcNode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcNode(String value) {
        this.srcNode = value;
    }

    /**
     * Recupera il valore della proprietà id.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Imposta il valore della proprietà id.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
