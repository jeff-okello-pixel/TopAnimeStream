package com.topanimestream.parallel;

public abstract class ParentCallback {

    /** The number of service calls that have successfully completed. */
    private int doneCount = 0;

    /** The children callbacks for which this parent checks to see if they are done. */
    @SuppressWarnings("unchecked")
    private ParallelCallback childCallbacks[];

    /**
     * Default constructor, passing in all child callbacks for the parent to check if they are done.
     */
    @SuppressWarnings("unchecked")
    protected ParentCallback(ParallelCallback... callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            throw new RuntimeException("No callbacks passed to parent");
        }

        this.childCallbacks = callbacks;

        for (ParallelCallback callback : callbacks) {
            callback.setParent(this);
        }
    }

    /**
     * Called by the child ParallelCallbacks on completion of the service call. Only when all
     * children have completed does this parent kick off it's on handleSuccess().
     */
    protected synchronized void done() {
        doneCount++;

        if (doneCount == childCallbacks.length) {
            handleSuccess();
        }

    }

    /**
     * Called only when all children callbacks have completed.
     */
    protected abstract void handleSuccess();

    /**
     * Get the data from the callback. Should only be called within the handleSuccess() block.
     */
    @SuppressWarnings("unchecked")
    protected <D extends Object> D getCallbackData(int index) {
        if (index < 0 || index >= childCallbacks.length) {
            throw new RuntimeException("Invalid child callback index");
        }

        return (D) childCallbacks[index].getData();
    }
}
