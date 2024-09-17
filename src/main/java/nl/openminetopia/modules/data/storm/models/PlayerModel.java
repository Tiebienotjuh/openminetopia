package nl.openminetopia.modules.data.storm.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "players")
public class PlayerModel extends StormModel {

    @Column(name = "uuid", unique = true)
    private UUID uniqueId;

    @Column(name = "level", defaultValue = "1")
    private Integer level;

    @Column(name = "playtime", defaultValue = "0")
    private Integer playtime;

    @Column(name = "active_prefix_id")
    private Integer activePrefixId;

    @Column(name = "active_prefixcolor_id")
    private Integer activePrefixColorId;

}