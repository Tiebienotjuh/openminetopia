package nl.openminetopia.modules.data.storm.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "prefixes")
public class PrefixesModel extends StormModel {

    @Column(name = "uuid", unique = true)
    private UUID uniqueId;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "color_id")
    private Integer colorId;

    @Column(name = "expires_at")
    private Timestamp expiresAt;
}
