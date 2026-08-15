package com.samos.osmand.domain.model.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("region", "", "")
data class RegionNode(
    val type: String? = null,
    val name: String? = null,
    val inner_download_suffix: String? = null,
    val inner_download_prefix: String? = null,

    // Recursive nesting: a region can contain sub-regions
    @XmlChildrenName("region", "", "")
    val subRegions: List<RegionNode> = emptyList()
)
