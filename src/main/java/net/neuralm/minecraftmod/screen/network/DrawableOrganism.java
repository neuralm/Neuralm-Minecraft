package net.neuralm.minecraftmod.screen.network;

import net.minecraft.util.ResourceLocation;
import net.neuralm.client.neat.ConnectionGene;
import net.neuralm.client.neat.Organism;
import net.neuralm.client.neat.nodes.AbstractNode;
import net.neuralm.client.neat.nodes.HiddenNode;
import net.neuralm.client.neat.nodes.InputNode;
import net.neuralm.client.neat.nodes.OutputNode;
import net.neuralm.minecraftmod.Neuralm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A wrapper around an organism that saves extra data to make drawing easier.
 */
public class DrawableOrganism {

    //The location of the node texture.
    static final ResourceLocation NODE_TEXTURE_LOCATION = new ResourceLocation(Neuralm.MODID, "textures/gui/node.png");

    //The organism to draw
    private final Organism organism;

    //The maximum layer in this organism, which will always be the inputs
    int maxLayer;
    //The nodes in this network wrapped in a more drawing friendly form
    private List<DrawableNode> nodes = new ArrayList<>();

    /**
     * Creates a new drawable organism and initializes all data needed to draw it.
     *
     * @param organism The organism to draw
     */
    DrawableOrganism(Organism organism) {
        this.organism = organism;

        //Need to initialize the organism so we have enough data to draw it
        initialize();
    }

    /**
     * Initializes the needed data to draw it
     * <p>
     * It creates all nodes needed and sets their layer
     */
    private void initialize() {
        //Create nodes
        for (ConnectionGene connectionGene : organism.getConnectionGenes()) {
            getNodeFromIdentifier(connectionGene.inNodeIdentifier);

            //This node has this gene as a connection coming in, so add it
            getNodeFromIdentifier(connectionGene.outNodeIdentifier).inConnections.add(connectionGene);
        }

        //Set all outputs to have a layer of 0. This will propagate through the network
        nodes.stream()
             .filter(node -> node.node instanceof OutputNode)
             .forEach(node -> node.setLayer(0));

        //Find the max layer so the nodes can know where to draw horizontally
        maxLayer = nodes.parallelStream()
                        .mapToInt(n -> n.layer)
                        .max()
                        .orElse(2);

        //Finally make sure the input nodes are in the same layer
        nodes.stream()
             .filter(n -> n.node instanceof InputNode)
             .forEach(n -> n.setLayer(maxLayer));

        //TODO: Find all nodes with the same layer and distribute them vertically
    }

    /**
     * Get a node with the given nodeIdentifier or create one if none exits.
     *
     * @param nodeIdentifier The node's identifier
     * @return A node with the given node identifier
     */
    DrawableNode getNodeFromIdentifier(int nodeIdentifier) {
        Optional<DrawableNode> nodes = this.nodes.stream()
                                                 .filter(n -> n.node.nodeIdentifier == nodeIdentifier)
                                                 .findAny();

        if (nodes.isPresent()) {
            return nodes.get();
        }

        Optional<InputNode> inputNode = organism.getInputNodes()
                                                .stream()
                                                .filter(n -> n.nodeIdentifier == nodeIdentifier)
                                                .findAny();

        if (inputNode.isPresent()) {
            return createAndAddNode(inputNode.get());
        }

        Optional<OutputNode> outputNode = organism.getOutputNodes()
                                                  .stream()
                                                  .filter(n -> n.nodeIdentifier == nodeIdentifier)
                                                  .findAny();

        if (outputNode.isPresent()) {
            return createAndAddNode(outputNode.get());
        }

        Optional<HiddenNode> hiddenNode = organism.getHiddenNodes()
                                                  .stream()
                                                  .filter(n -> n.nodeIdentifier == nodeIdentifier)
                                                  .findAny();

        return hiddenNode.map(this::createAndAddNode)
                         .orElseThrow(() -> new IllegalArgumentException(
                                 "Could not find a node with the given nodeIdentifier"));

    }

    private DrawableNode createAndAddNode(AbstractNode n) {
        double y;

        if (n instanceof InputNode) {
            //y position is based on its nodeidentifier and the amount of input nodes.
            //Each node has the same distance between it and its neighbours (or wall).
            //So each node has 1/(inputcount + 1) units between eachother.
            //The nodes also start half a unit away from the wall

            y = (1.0 / (organism.getInputCount() + 1)) * n.nodeIdentifier + (1.0 / organism.getInputCount()) / 2;
        } else if (n instanceof OutputNode) {
            //Same as with the input node, except the nodeidentifier goes from inputCount to outputCount-1, so we need to substract inputCount to go between 0 and outputCount-1

            y = (1.0 / (organism.getOutputCount() + 1)) * (n.nodeIdentifier - organism.getInputCount()) + (1.0 / organism.getOutputCount()) / 2;
        } else {
            //TODO: Make hidden nodes spread out evenly vertically instead of randomly
            y = Math.random();
        }

        DrawableNode node = new DrawableNode(y, n, this);
        nodes.add(node);

        return node;
    }

    /**
     * Draw the network.
     *
     * @param gui The gui that wants to draw this network
     */
    void draw(NetworkScreen gui) {
        drawNodes(gui);
    }

    /**
     * Draw the nodes in the network.
     * The nodes also draw the connection genes connected to them
     *
     * @param gui The gui that wants to draw this network.
     */
    private void drawNodes(NetworkScreen gui) {
        final int nodeSize = 16;

        //Reduce the width and height so we don't get nodes going outside of the walls at default zoom level
        int width = gui.width - nodeSize * 3;
        int height = gui.height - nodeSize * 2;

        for (DrawableNode node : nodes) {
            node.draw(width, height);
        }
    }

}
