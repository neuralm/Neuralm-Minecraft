package net.neuralm.minecraftmod.neuralmhelpers;

import net.minecraft.network.PacketBuffer;
import net.neuralm.client.neat.ConnectionGene;
import net.neuralm.client.neat.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class OrganismByteHelper {

    /**
     * Write the given organism to the given buffer.
     *
     * @param buffer   The buffer to write to
     * @param organism The organism to write
     */
    public static void writeOrganism(PacketBuffer buffer, Organism organism) {
        buffer.writeString(organism.getName());
        buffer.writeInt(organism.getGeneration());
        buffer.writeDouble(organism.getScore());
        buffer.writeUniqueId(organism.getId());
        buffer.writeInt(organism.getInputCount());
        buffer.writeInt(organism.getOutputCount());

        List<ConnectionGene> connectionGenes = organism.getConnectionGenes();
        buffer.writeInt(connectionGenes.size());
        for (ConnectionGene connectionGene : connectionGenes) {
            writeConnectionGene(buffer, connectionGene);
        }
    }

    /**
     * Write the given connection gene to the given buffer
     *
     * @param buffer         The buffer to write to
     * @param connectionGene The connection gene to write
     */
    public static void writeConnectionGene(PacketBuffer buffer, ConnectionGene connectionGene) {
        buffer.writeInt(connectionGene.inNodeIdentifier);
        buffer.writeInt(connectionGene.outNodeIdentifier);
        buffer.writeInt(connectionGene.getInnovationNumber());
        buffer.writeBoolean(connectionGene.enabled);
        buffer.writeDouble(connectionGene.weight);
        buffer.writeUniqueId(connectionGene.getId());
        buffer.writeUniqueId(connectionGene.getOrganismId());
    }

    /**
     * Read an organism from the given buffer.
     * This will fail if the organism was not written in the same way as {@link OrganismByteHelper#writeOrganism(PacketBuffer, Organism)}
     *
     * @param buffer The buffer to read from
     * @return An organism based on the data in the buffer
     */
    public static Organism readOrganism(PacketBuffer buffer) {
        String name = buffer.readString();
        int generation = buffer.readInt();
        double score = buffer.readDouble();
        UUID organismId = buffer.readUniqueId();
        int inputCount = buffer.readInt();
        int outputCount = buffer.readInt();

        int amountOfGenes = buffer.readInt();
        List<ConnectionGene> genes = new ArrayList<>(amountOfGenes);
        for (int i = 0; i < amountOfGenes; i++) {
            genes.add(readConnectionGene(buffer));
        }

        return new Organism(genes, inputCount, outputCount, organismId, score, name, generation);
    }

    /**
     * Read a connection gene from the given buffer.
     * This will fail if the connection gene was not written in the same way as {@link OrganismByteHelper#writeConnectionGene(PacketBuffer, ConnectionGene)}
     *
     * @param buffer The buffer to read from
     * @return A connection gene based on the data in the buffer
     */
    public static ConnectionGene readConnectionGene(PacketBuffer buffer) {
        int inNodeIdentifier = buffer.readInt();
        int outNodeIdentifier = buffer.readInt();
        int innovationNumber = buffer.readInt();
        boolean enabled = buffer.readBoolean();
        double weight = buffer.readDouble();
        UUID id = buffer.readUniqueId();
        UUID organismId = buffer.readUniqueId();

        return new ConnectionGene(inNodeIdentifier, outNodeIdentifier, weight, enabled, innovationNumber, id,
                                  organismId);
    }
}
