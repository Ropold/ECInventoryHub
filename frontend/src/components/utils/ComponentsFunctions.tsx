import type {AxiosError} from "axios";
import {useEffect} from "react";
import {useLocation} from "react-router-dom";
import type {DeviceModel} from "../models/DeviceModel.ts";

export function onFileChange(
    e: React.ChangeEvent<HTMLInputElement>,
    setImage: (file: File | null) => void
) {
    if (e.target.files) {
        const file = e.target.files[0];
        setImage(file);
    }
}

export function onImageCancel(setImage: (file: File | null) => void) {
    setImage(null);
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
        fileInput.value = '';
    }
}

export function formatDate(dateString: string | undefined): string {
    if (!dateString) return "—";
    return new Date(dateString).toLocaleDateString('de-DE');
}

export function getDeviceLabel(device: DeviceModel): string {
    let candidates: (string | null)[];
    switch (device.type) {
        case "LAPTOP":
            candidates = [device.hostname, device.serialNumber, device.inventoryNumber];
            break;
        case "PHONE":
        case "TABLET":
            candidates = [device.serialNumber, device.inventoryNumber, device.hostname];
            break;
        default:
            candidates = [device.inventoryNumber, device.serialNumber, device.hostname];
    }
    const label = candidates.find((value) => value && value.trim() !== "");
    if (label) return label;
    return [device.manufacturer, device.modelName].filter(Boolean).join(" ") || "—";
}

export const useAutoScrollToTop = () => {
    const location = useLocation();
    useEffect(() => {
        window.scroll(0, 0);
    }, [location]);
};


export function renderImagePreview(
    image: File | null,
    existingImageUrl: string | undefined,
    imageDeleted: boolean
) {
    if (image) {
        return (<img src={URL.createObjectURL(image)} alt="image-preview" className="image-preview" />);
    }
    if (existingImageUrl && !imageDeleted) {
        return (<img src={existingImageUrl} alt="existing-image" className="image-preview" />);
    }
    return null;
}
export function handleRequestError(
    error: AxiosError<{message?: string; details?: string[]}>,
    loginMessage: string,
    genericMessage: string,
    setError: (message: string | null) => void,
    setBlockingItems?: (items: string[]) => void
) {
    const status = error.response?.status;

    if (status === 401 || status === 403) {
        setError(loginMessage);
        setBlockingItems?.([]);
    } else if (status === 409) {
        setError(error.response?.data?.message ?? genericMessage);
        setBlockingItems?.(error.response?.data?.details ?? []);
    } else {
        setError(genericMessage);
        setBlockingItems?.([]);
    }
}

export function renderBlockingList(items: string[]) {
    if (items.length === 0) return null;
    return (
        <ul className="popup-error-list">
            {items.map((item) => (
                <li key={item}>{item}</li>
            ))}
        </ul>
    );
}
