package com.ibm.health.vaccination.sdk.android.qr

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.Test

public class QRCoderTest {

    /**
     * Makes sure we have a correct decoding a data from some QR code to the ValidationCertificate.
     */
    @Test
    public fun `check data fields after decoding`() {
        val qrDataModel = QRCoder().decodeVaccinationCert(RAW_VACCINATION_QR_DATA)
        assertThat(qrDataModel).isNotEqualTo(null)
        assertThat(qrDataModel.name).isEqualTo(TEST_NAME_IN_QR_DATA)
        assertThat(qrDataModel.vaccination[0].country).isEqualTo(TEST_COUNTRY_IN_QR_DATA)
    }

    /**
     * Makes sure we have a correct decoding a data from some QR code to the ValidationCertificate.
     */
    @Test
    public fun `check data fields after decoding d`() {
        val qrDataModel = QRCoder().decodeValidationCert(RAW_VALIDATION_QR_DATA)
        assertThat(qrDataModel).isNotEqualTo(null)
        assertThat(qrDataModel.name).isEqualTo(TEST_NAME_IN_QR_DATA)
        assertThat(qrDataModel.vaccination[0].country).isEqualTo(TEST_COUNTRY_IN_QR_DATA)
    }

    public companion object {
        public val RAW_VACCINATION_QR_DATA: String = "6BFXZ8-VN+J28S24DBT%MW5P+V09DH3E1ENIGCTEGJMZG0-4-63 6AHPO*." +
            "TD*RH0DQ-4+99Y RXIIKT2G50Q06\$Y2LT04+68+M02M0M2AWM1NMU:3RMC R9I 7S95.-7:\$UD7Q73NTECM+D/EP*VO*CMITA9" +
            " 5VQLMNHBDDN 8RPO*KQ:HHFA3OJJ+OSKM3/.N-OPWG2K\$MQ 4KS8FPKTDQ+VF4GN2.GIXTXMGB.L0:GR L8C7679.6JMFSGXG3" +
            "VGOMJ2K2ZCNXVGTCGBYJ.OP5G49T3\$UFXG9OT75*ES10-CO2H4OZG09W458F-C.1PLQB0-CPVCPMARVM3I9MXN7\$DZXTK/0\$2" +
            "4GC9WH8GF5+T5 YD:CN950::2+CWG%3+%G*GTP:EJN18G2JWI1+3:PMS.G731Y9AOX6LWST+KR58+JO4CI%EAE*VE0LOG87W68JL" +
            "MOJQNC\$+C/5RAE8 TKOE38HETHL+VBWP4HBHN4HWR9RIJR5A:OL*ZF/J8 -T7YO7C0%UB+XIIGBSDWYQPZXN\$-R9+TOU6Z:3YA" +
            "R1TR8DAUWN+IB\$0V69KPE9TMR1DHNO3O:SVYSGU6K:JM+SLNJP3S4U8BTEFD2XJK/U4-2409BHO62MC:6C52UUSLY%5O.V+FW-" +
            "\$2*MAVYMR8SK2G+ZUSCRH:SQ6D+JUCMTD14W/DF.5KUUHBSRFVILVN8SN/LPDTYMF7%1K3UL.004IB17F6II2GUV1LMDY4"
        public val RAW_VALIDATION_QR_DATA: String = "6BFVZ8JA67I0/20A82CN0H.4IT0/MH**KMJN83UM0DI\$APZD7\$L9-L7-51" +
            ".M0QFE1QUID-O545WQGEKMG:24\$37Y2MHVK\$01Y/9G24\$ 14X02/SV6IGEV5\$V4UQJGCGVC1SDQ\$M4HG3/G996URKE/RW.M" +
            "JA6VGDXB8IZQOWI1LM+\$F\$6U-U3YFMY*6YHGDCJDHM9JKU*S/\$PQ%521R/LH8HK*RLJJ0S:U5*4IL1SCNYQR84M.QMF I4*FA" +
            "-F9P2PUFK3I\$F4P6SSXL+84FHIXB0/7M7J9TZ0PQUZ/QLLFJY49.BSVEZGGREH3S48:LDU40:A:A3%:GU3G7:2+GQYODTA7XTI2" +
            "NV5SHF\$RM\$K+L1VML0EQHGTD*GTFG/ME92N6+162WXKPIZ2*H5TUUCTLGHHBRNCKA3VN3TEPJGYKBEEL47PWPHS7KUKBZ+VZA6" +
            "X+P744C46C 22NTGI72C8NE0NBT.\$9EISM2437E975L7IRU60C78U710UTHH17V6PUO\$7XMV3NJXH7A+FVX95AOK6I4 V-\$10" +
            "YPYMBT1D7-J76OOAE1*BN-IB5F9:25XPTN84ATUEW/AWK80IFJ"
        public val TEST_NAME_IN_QR_DATA: String = "Mustermann Erika"
        public val TEST_COUNTRY_IN_QR_DATA: String = "DE"
    }
}
