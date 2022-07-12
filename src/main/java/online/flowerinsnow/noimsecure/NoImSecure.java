package online.flowerinsnow.noimsecure;


import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ActionResult;
import online.flowerinsnow.noimsecure.config.NoImSecureConfig;
import online.flowerinsnow.noimsecure.eci.PlayerPublicKeyReadCallback;
import online.flowerinsnow.noimsecure.eci.PlayerPublicKeyVerfyingCallback;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NoImSecure implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public void onInitialize() {
        Path config = Path.of(FabricLoader.getInstance().getConfigDir().toString(), "no_imsecure.config.xml");
        try (InputStream defaultConfig = NoImSecure.class.getResourceAsStream("/no_imsecure.config.xml")) {
            if (defaultConfig == null) {
                LOGGER.error("Cannot get resource \"/no_imsecure.config.xml\"");
            } else {
                Files.copy(defaultConfig, config);
            }
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException ex) {
            LOGGER.error("Coping default configuration file.", ex);
        }

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(config.toFile());
            Element element = document.getDocumentElement();
            NodeList childs = element.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node node = childs.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().equals("secure")) {
                        readSecureNode((Element) node);
                    } else if (node.getNodeName().equals("render")) {
                        readRenderNode((Element) node);
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOGGER.error("Parsing configuration file.", e);
        }

        PlayerPublicKeyReadCallback.EVENT.register((packet) -> NoImSecureConfig.Secure.NoPublicKey.read ? ActionResult.SUCCESS : ActionResult.PASS);
        PlayerPublicKeyVerfyingCallback.EVENT.register((server, profile, publicKeyData, connection) -> NoImSecureConfig.Secure.NoPublicKey.read ? ActionResult.SUCCESS : ActionResult.PASS);
    }

    private static void readSecureNode(Element element) {
        NodeList childs = element.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("noPublicKey")) {
                NodeList nodes = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    Node cn = nodes.item(i);
                    if (cn.getNodeType() == Node.TEXT_NODE) {
                        if (cn.getNodeName().equals("read")) {
                            NoImSecureConfig.Secure.NoPublicKey.read = Boolean.parseBoolean(cn.getNodeValue());
                        } else if (cn.getNodeName().equals("write")) {
                            NoImSecureConfig.Secure.NoPublicKey.write = Boolean.parseBoolean(cn.getNodeValue());
                        }
                    }
                }
                return;
            }
        }
    }

    private static void readRenderNode(Element element) {
        NodeList childs = element.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("noInsecure")) {
                NodeList nodes = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    Node cn = nodes.item(i);
                    if (cn.getNodeType() == Node.TEXT_NODE) {
                        if (cn.getNodeName().equals("server")) {
                            NoImSecureConfig.Render.NoInsecure.server = Boolean.parseBoolean(cn.getNodeValue());
                        } else if (cn.getNodeName().equals("chat")) {
                            NoImSecureConfig.Render.NoInsecure.chat = Boolean.parseBoolean(cn.getNodeValue());
                        }
                    }
                }
                return;
            }
        }
    }
}
