package com.samos.osmand.domain.model.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("regions_list", "", "")
data class RegionsListXml(
    // Looks for nested <region> tags inside <regions_list>
    @XmlSerialName("region", "", "")
    val regions: List<RegionNode> = emptyList()
)
