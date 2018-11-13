package chat.model.repository;

import chat.model.entity.Rank;
import chat.util.JSONParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Alexander Diachenko.
 */
@Repository
public class JSONRankRepository implements RankRepository {

    private final static Logger logger = LogManager.getLogger(JSONRankRepository.class);

    private ObjectMapper mapper = new ObjectMapper();
    private Set<Rank> ranks;
    private String path;

    public JSONRankRepository() {
    }

    public JSONRankRepository(String path) {
        this.path = path;
        getAll();
    }

    @Override
    public Set<Rank> getAll() {
        try {
            this.ranks = new TreeSet<>(new HashSet<>(
                    this.mapper.readValue(JSONParser.readFile(this.path), new TypeReference<List<Rank>>() {
                    })));
            return this.ranks;
        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
        return new TreeSet<>();
    }

    @Override
    public Rank add(Rank rank) {
        this.ranks.add(rank);
        flush();
        return rank;
    }

    @Override
    public Rank update(Rank rank) {
        this.ranks.remove(rank);
        this.ranks.add(rank);
        flush();
        return rank;
    }

    @Override
    public Rank delete(Rank rank) {
        this.ranks.remove(rank);
        flush();
        return rank;
    }

    @Override
    public Rank getRankByExp(long exp) {
        Rank nearest = new Rank();
        for (Rank rank : this.ranks) {
            int rankExp = rank.getExp();
            if (rankExp <= exp) {
                nearest = rank;
            }
        }
        return nearest;
    }

    @Override
    public boolean isNewRank(long exp) {
        Rank rankByExp = getRankByExp(exp);
        return rankByExp.getExp() == exp;
    }

    private void flush() {
        Thread thread = new Thread(() -> {
            synchronized (this) {
                try {
                    this.mapper.writeValue(new FileOutputStream(this.path), this.ranks);
                } catch (IOException exception) {
                    logger.error(exception.getMessage(), exception);
                    throw new RuntimeException("Ranks failed to save. Create " + this.path, exception);
                }
            }
        });
        thread.start();
    }
}
