package com.samos.osmand.domain.model.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("region", "", "")
data class RegionNode(
    // 💡 Main structural fields used for hierarchy and file naming
    val type: String? = null,
    val name: String? = null,
    val inner_download_suffix: String? = null,
    val inner_download_prefix: String? = null,
    val download_suffix: String? = null,
    val download_prefix: String? = null,

    // 💡 Localization and translation attributes
    val translate: String? = null,
    val lang: String? = null,

    // 💡 Map data types configuration switches
    val map: String? = null,
    val srtm: String? = null,
    val hillshade: String? = null,
    val wiki: String? = null,
    val roads: String? = null,
    val address: String? = null,

    // 💡 File merging preferences
    val join_map_files: String? = null,
    val join_road_files: String? = null,
    val join_srtm_files: String? = null,
    val join_wiki_files: String? = null,

    // 💡 Geographic, Extraction and Political attributes
    val poly_extract: String? = null,
    val boundary: String? = null,
    val disputed: String? = null,
    val com_osmand_poly_extracted: String? = null,

    // 💡 Navigation and specialized attributes
    val left_hand_navigation: String? = null,
    val driving_side: String? = null,
    val continent: String? = null,
    val country: String? = null,
    val srtm_hillshade: String? = null,
    // 💡 FIX: Added missing metric attribute from line 371
    val metric: String? = null,

    // 💡 Coordinates and bounding box attributes
    val lat: String? = null,
    val lon: String? = null,
    val minlat: String? = null,
    val minlon: String? = null,
    val maxlat: String? = null,
    val maxlon: String? = null,

    // 💡 Metadata, indexing and web control attributes
    val zoom: String? = null,
    val search_index: String? = null,
    val web_download: String? = null,
    val parent_id: String? = null,
    val map_suffix: String? = null,
    val roads_suffix: String? = null,

    // Recursive nesting for children <region> tags
    @XmlSerialName("region", "", "")
    val subRegions: List<RegionNode> = emptyList()
)
