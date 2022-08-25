package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part16/4.4.8">https://reference.opcfoundation.org/v105/Core/docs/Part16/4.4.8</a>
 */
public interface StateType extends BaseObjectType {
    QualifiedProperty<UInteger> STATE_NUMBER = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "StateNumber",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=7"),
        -1,
        UInteger.class
    );

    UInteger getStateNumber();

    void setStateNumber(UInteger value);

    PropertyType getStateNumberNode();
}