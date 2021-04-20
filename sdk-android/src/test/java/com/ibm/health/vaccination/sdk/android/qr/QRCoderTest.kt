package com.ibm.health.vaccination.sdk.android.qr

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test

public class QRCoderTest {

    /**
     * Makes sure we have a correct decoding a data from some QR code to the ValidationCertificate.
     */
    @ExperimentalSerializationApi
    @Test
    public fun `check data fields after decoding`() {
        val qrDataModel = QRCoder().decode(RAW_QR_DATA)
        assertThat(qrDataModel).isNotEqualTo(null)
        assertThat(qrDataModel.name).isEqualTo(TEST_NAME_IN_QR_DATA)
        assertThat(qrDataModel.vaccination[0].country).isEqualTo(TEST_COUNTRY_IN_QR_DATA)
    }

    public companion object {
        public val RAW_QR_DATA: String = "6BFXZ8-VN+J28S24DBT%MW5P+V09DH3E1ENIGCTEGJMZG0-4-63 6AHPO*.TD*RH0D" +
            "Q-4+99Y RXIIKT2G50Q06\$Y2LT04+68+M02M0M2AWM1NMU:3RMC R9I 7S95.-7:\$UD7Q73NTECM+D/EP*VO*CMITA9 5VQLMNH" +
            "BDDN 8RPO*KQ:HHFA3OJJ+OSKM3/.N-OPWG2K\$MQ 4KS8FPKTDQ+VF4GN2.GIXTXMGB.L0:GR L8C7679.6JMFSGXG3VGOMJ2K2Z" +
            "CNXVGTCGBYJ.OP5G49T3\$UFXG9OT75*ES10-CO2H4OZG09W458F-C.1PLQB0-CPVCPMARVM3I9MXN7\$DZXTK/0\$24GC9WH8GF5" +
            "+T5 YD:CN950::2+CWG%3+%G*GTP:EJN18G2JWI1+3:PMS.G731Y9AOX6LWST+KR58+JO4CI%EAE*VE0LOG87W68JLMOJQNC\$+C/" +
            "5RAE8 TKOE38HETHL+VBWP4HBHN4HWR9RIJR5A:OL*ZF/J8 -T7YO7C0%UB+XIIGBSDWYQPZXN\$-R9+TOU6Z:3YAR1TR8DAUWN+I" +
            "B\$0V69KPE9TMR1DHNO3O:SVYSGU6K:JM+SLNJP3S4U8BTEFD2XJK/U4-2409BHO62MC:6C52UUSLY%5O.V+FW-\$2*MAVYMR8SK2" +
            "G+ZUSCRH:SQ6D+JUCMTD14W/DF.5KUUHBSRFVILVN8SN/LPDTYMF7%1K3UL.004IB17F6II2GUV1LMDY4"
        public val TEST_NAME_IN_QR_DATA: String = "Mustermann Erika"
        public val TEST_COUNTRY_IN_QR_DATA: String = "DE"
    }
}
