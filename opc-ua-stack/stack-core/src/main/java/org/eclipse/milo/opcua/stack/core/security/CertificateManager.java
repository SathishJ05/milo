package org.eclipse.milo.opcua.stack.core.security;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.jetbrains.annotations.Nullable;

public interface CertificateManager {

    /**
     * Get the {@link KeyPair} belonging to the certificate identified by {@code thumbprint}.
     * <p>
     * {@code thumbprint} is a SHA1 hash of the encoded certificate.
     *
     * @param thumbprint the thumbprint of the certificate.
     * @return the {@link KeyPair} belonging to the certificate identified by {@code thumbprint}.
     */
    Optional<KeyPair> getKeyPair(ByteString thumbprint);

    /**
     * Get the {@link X509Certificate} identified by {@code thumbprint}.
     * <p>
     * {@code thumbprint} is a SHA1 hash of the encoded certificate.
     *
     * @param thumbprint the thumbprint of the certificate.
     * @return the {@link X509Certificate} identified by {@code thumbprint}.
     */
    Optional<X509Certificate> getCertificate(ByteString thumbprint);

    /**
     * Get the {@link X509Certificate} identified by {@code thumbprint} as well as any
     * certificates in its chain.
     *
     * @param thumbprint the thumbprint of the certificate.
     * @return the {@link X509Certificate} identified by {@code thumbprint} as well as any
     *     certificates in its chain.
     */
    Optional<X509Certificate[]> getCertificateChain(ByteString thumbprint);

    /**
     * Get the {@link CertificateGroup} containing the {@link X509Certificate} identified by
     * {@code thumbprint}.
     *
     * @param thumbprint the thumbprint of the certificate.
     * @return the {@link CertificateGroup} containing the {@link X509Certificate} identified by
     *     {@code thumbprint}.
     */
    Optional<CertificateGroup> getCertificateGroup(ByteString thumbprint);

    /**
     * Get the {@link CertificateGroup} identified by {@code certificateGroupId}.
     *
     * @param certificateGroupId the {@link NodeId} identifying the {@link CertificateGroup}.
     * @return the {@link CertificateGroup} identified by {@code certificateGroupId}.
     */
    Optional<CertificateGroup> getCertificateGroup(NodeId certificateGroupId);

    /**
     * Get the {@link CertificateGroup}s managed by this {@link CertificateManager}.
     *
     * @return the {@link CertificateGroup}s managed by this {@link CertificateManager}.
     */
    List<CertificateGroup> getCertificateGroups();

    /**
     * Get the DefaultApplicationGroup {@link CertificateGroup}, if it's configured.
     * <p>
     * Servers are required to support the DefaultApplicationGroup CertificateGroup.
     *
     * @return the DefaultApplicationGroup {@link CertificateGroup}, if it's configured.
     */
    default Optional<CertificateGroup> getDefaultApplicationGroup() {
        return getCertificateGroup(NodeIds.ServerConfiguration_CertificateGroups_DefaultApplicationGroup);
    }

    /**
     * Get the DefaultUserTokenGroup {@link CertificateGroup}, if it's configured.
     * <p>
     * Support for the DefaultUserTokenGroup CertificateGroup is optional.
     *
     * @return the DefaultUserTokenGroup {@link CertificateGroup}, if it's configured.
     */
    default Optional<CertificateGroup> getDefaultUserTokenGroup() {
        return getCertificateGroup(NodeIds.ServerConfiguration_CertificateGroups_DefaultUserTokenGroup);
    }

    /**
     * Get the DefaultHttpsGroup {@link CertificateGroup}, if it's configured.
     * <p>
     * Support for the DefaultHttpsGroup CertificateGroup is optional.
     *
     * @return the DefaultHttpsGroup {@link CertificateGroup}, if it's configured.
     */
    default Optional<CertificateGroup> getDefaultHttpsGroup() {
        return getCertificateGroup(NodeIds.ServerConfiguration_CertificateGroups_DefaultHttpsGroup);
    }

    /**
     * Get the list of Rejected Certificates.
     *
     * @return the list of Rejected {@link X509Certificate}s.
     */
    List<X509Certificate> getRejectedCertificates();

    /**
     * Add {@code certificate} to the Rejected Certificates list.
     *
     * @param certificate the {@link X509Certificate} to add to the Rejected Certificates list.
     */
    void addRejectedCertificate(X509Certificate certificate);

    /**
     * Remove {@code certificate} from the Rejected Certificates list.
     *
     * @param certificate the {@link X509Certificate} to remove from the Rejected Certificates list.
     * @return {@code true} if the certificate was removed.
     */
    boolean removeRejectedCertificate(X509Certificate certificate);

    interface CertificateGroup {

        /**
         * Get the {@link NodeId} identifying this {@link CertificateGroup}.
         *
         * @return the {@link NodeId} identifying this {@link CertificateGroup}.
         */
        NodeId getCertificateGroupId();

        /**
         * Get the {@link NodeId}s identifying the types of certificates supported by this
         * {@link CertificateGroup}.
         *
         * @return the {@link NodeId}s identifying the types of certificates supported by this
         *     {@link CertificateGroup}.
         */
        List<NodeId> getSupportedCertificateTypeIds();

        /**
         * Get the {@link TrustListManager} for this {@link CertificateGroup}.
         *
         * @return the {@link TrustListManager} for this {@link CertificateGroup}.
         */
        TrustListManager getTrustListManager();

        /**
         * Get the {@link CertificateRecord}s belonging to this {@link CertificateGroup}.
         *
         * @return the {@link CertificateRecord}s belonging to this {@link CertificateGroup}.
         */
        List<CertificateRecord> getCertificateRecords();

        /**
         * Get the {@link KeyPair} associated with the certificate of the type identified by
         * {@code certificateTypeId}.
         *
         * @param certificateTypeId the {@link NodeId} identifying the type of certificate.
         * @return the {@link KeyPair} associated with the certificate of the type identified by
         *     {@code certificateTypeId}.
         */
        Optional<KeyPair> getKeyPair(NodeId certificateTypeId);

        /**
         * Get the {@link X509Certificate} chain associated with the certificate of the type
         * identified by {@code certificateTypeId}.
         *
         * @param certificateTypeId the {@link NodeId} identifying the type of certificate.
         * @return the {@link X509Certificate} chain associated with the certificate of the type
         *     identified by {@code certificateTypeId}.
         */
        Optional<X509Certificate[]> getCertificateChain(NodeId certificateTypeId);

        /**
         * Update the {@link KeyPair} and {@link X509Certificate} associated with the type
         * identified by {@code certificateTypeId}.
         *
         * @param certificateTypeId the {@link NodeId} identifying the type of certificate.
         * @param keyPair the new {@link KeyPair}.
         * @param certificateChain the new {@link X509Certificate} chain.
         * @throws Exception if the update fails.
         */
        void updateCertificate(
            NodeId certificateTypeId,
            KeyPair keyPair,
            X509Certificate[] certificateChain
        ) throws Exception;

        /**
         * Get the {@link ServerCertificateValidator} for this {@link CertificateGroup}.
         *
         * @return the {@link ServerCertificateValidator} for this {@link CertificateGroup}.
         */
        ServerCertificateValidator getCertificateValidator();

        class CertificateRecord {
            public final NodeId certificateGroupId;
            public final NodeId certificateTypeId;
            public final X509Certificate[] certificateChain;

            public CertificateRecord(
                NodeId certificateGroupId,
                NodeId certificateTypeId,
                X509Certificate[] certificateChain
            ) {

                this.certificateGroupId = certificateGroupId;
                this.certificateTypeId = certificateTypeId;
                this.certificateChain = certificateChain;
            }
        }

    }

    interface CertificateFactory {

        /**
         * Create a {@link KeyPair} for the certificate of the type identified by
         * {@code certificateTypeId}.
         *
         * @param certificateTypeId the {@link NodeId} identifying the type of certificate.
         * @return the new {@link KeyPair}.
         */
        KeyPair createKeyPair(NodeId certificateTypeId);

        /**
         * Create a {@link X509Certificate} chain for the certificate of the type identified by
         * {@code certificateTypeId}.
         *
         * @param certificateTypeId the {@link NodeId} identifying the type of certificate.
         * @param keyPair the {@link KeyPair} to use when creating the certificate chain.
         * @return the new {@link X509Certificate} chain.
         */
        X509Certificate[] createCertificateChain(NodeId certificateTypeId, KeyPair keyPair);

    }

    class DefaultApplicationGroup implements CertificateGroup {

        private final AtomicBoolean initialized = new AtomicBoolean(false);

        private final ServerCertificateValidator certificateValidator;

        private final KeyManager keyManager;
        private final TrustListManager trustListManager;
        private final CertificateFactory certificateFactory;

        public DefaultApplicationGroup(
            KeyManager keyManager,
            TrustListManager trustListManager,
            CertificateFactory certificateFactory
        ) {

            this.keyManager = keyManager;
            this.trustListManager = trustListManager;
            this.certificateFactory = certificateFactory;

            certificateValidator = new DefaultServerCertificateValidator(trustListManager);
        }

        public void initialize() throws Exception {
            if (initialized.compareAndSet(false, true)) {
                for (NodeId certificateTypeId : getSupportedCertificateTypeIds()) {
                    String alias = getAlias(certificateTypeId);

                    if (!keyManager.contains(alias)) {
                        KeyPair keyPair = certificateFactory.createKeyPair(certificateTypeId);
                        X509Certificate[] certificateChain =
                            certificateFactory.createCertificateChain(certificateTypeId, keyPair);

                        keyManager.set(
                            alias,
                            getPassword(certificateTypeId),
                            new KeyManager.KeyRecord(keyPair.getPrivate(), certificateChain)
                        );
                    }
                }
            }
        }

        @Override
        public NodeId getCertificateGroupId() {
            return NodeIds.ServerConfiguration_CertificateGroups_DefaultApplicationGroup;
        }

        @Override
        public List<NodeId> getSupportedCertificateTypeIds() {
            return List.of(NodeIds.RsaSha256ApplicationCertificateType);
        }

        @Override
        public TrustListManager getTrustListManager() {
            return trustListManager;
        }

        @Override
        public List<CertificateRecord> getCertificateRecords() {
            var certificateRecords = new ArrayList<CertificateRecord>();

            for (NodeId certificateTypeId : getSupportedCertificateTypeIds()) {
                String alias = getAlias(certificateTypeId);
                String password = getPassword(certificateTypeId);

                if (alias != null && password != null) {
                    try {
                        KeyManager.KeyRecord keyRecord = keyManager.get(alias, password);

                        if (keyRecord != null) {
                            certificateRecords.add(new CertificateRecord(
                                getCertificateGroupId(),
                                certificateTypeId,
                                keyRecord.certificateChain
                            ));
                        }
                    } catch (Exception e) {
                        return List.of();
                    }
                }
            }

            return certificateRecords;
        }

        @Override
        public Optional<KeyPair> getKeyPair(NodeId certificateTypeId) {
            if (certificateTypeId.equals(NodeIds.RsaSha256ApplicationCertificateType)) {
                String alias = getAlias(certificateTypeId);
                String password = getPassword(certificateTypeId);

                if (alias == null || password == null) {
                    return Optional.empty();
                }

                try {
                    KeyManager.KeyRecord keyRecord = keyManager.get(alias, password);

                    return Optional.ofNullable(keyRecord)
                        .map(r -> new KeyPair(r.certificateChain[0].getPublicKey(), r.privateKey));
                } catch (Exception e) {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<X509Certificate[]> getCertificateChain(NodeId certificateTypeId) {
            if (certificateTypeId.equals(NodeIds.RsaSha256ApplicationCertificateType)) {
                String alias = getAlias(certificateTypeId);
                String password = getPassword(certificateTypeId);

                if (alias == null || password == null) {
                    return Optional.empty();
                }

                try {
                    KeyManager.KeyRecord keyRecord = keyManager.get(alias, password);

                    return Optional.ofNullable(keyRecord).map(r -> r.certificateChain);
                } catch (Exception e) {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void updateCertificate(
            NodeId certificateTypeId,
            KeyPair keyPair,
            X509Certificate[] certificateChain
        ) throws Exception {

            if (certificateTypeId.equals(NodeIds.RsaSha256ApplicationCertificateType)) {
                String alias = getAlias(certificateTypeId);
                String password = getPassword(certificateTypeId);

                if (alias == null || password == null) {
                    return;
                }

                keyManager.set(alias, password, new KeyManager.KeyRecord(keyPair.getPrivate(), certificateChain));
            } else {
                throw new UaException(StatusCodes.Bad_InvalidArgument, "certificateTypeId");
            }
        }

        @Override
        public ServerCertificateValidator getCertificateValidator() {
            return certificateValidator;
        }

        protected @Nullable String getAlias(NodeId certificateTypeId) {
            if (certificateTypeId.equals(NodeIds.RsaSha256ApplicationCertificateType)) {
                return "server-rsa-sha256";
            } else {
                return null;
            }
        }

        protected @Nullable String getPassword(NodeId certificateTypeId) {
            if (certificateTypeId.equals(NodeIds.RsaSha256ApplicationCertificateType)) {
                return "password";
            } else {
                return null;
            }
        }

        /**
         * Create and initialize a {@link DefaultApplicationGroup}.
         *
         * @param keyManager the {@link KeyManager} to use.
         * @param trustListManager the {@link TrustListManager} to use.
         * @param certificateFactory the {@link CertificateFactory} to use.
         * @return an initialized {@link DefaultApplicationGroup} instance.
         * @throws Exception if an error occurs while initializing the
         *     {@link DefaultApplicationGroup}.
         */
        public static DefaultApplicationGroup createAndInitialize(
            KeyManager keyManager,
            TrustListManager trustListManager,
            CertificateFactory certificateFactory
        ) throws Exception {

            var defaultApplicationGroup = new DefaultApplicationGroup(
                keyManager,
                trustListManager,
                certificateFactory
            );

            defaultApplicationGroup.initialize();

            return defaultApplicationGroup;
        }

    }

}
