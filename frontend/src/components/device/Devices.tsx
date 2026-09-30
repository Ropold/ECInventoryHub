import type {DeviceModel} from "../models/DeviceModel.ts";
import {useState} from "react";
import {useAutoScrollToTop} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import DeviceCard from "./DeviceCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type DeviceProps = {
    language: string;
    role: string;
    devices: DeviceModel[];
}

function filterDevices(devices: DeviceModel[], query: string): DeviceModel[] {
    if (!devices) return [];

    const searchQuery = query.toLowerCase();

    return devices.filter(device => {
        return (
            device.manufacturer?.toLowerCase().includes(searchQuery) ||
            device.modelName?.toLowerCase().includes(searchQuery) ||
            device.serialNumber?.toLowerCase().includes(searchQuery) ||
            device.inventoryNumber?.toLowerCase().includes(searchQuery) ||
            device.type.toLowerCase().includes(searchQuery) ||
            device.status.toLowerCase().includes(searchQuery) ||
            device.location?.name.toLowerCase().includes(searchQuery) ||
            device.notes?.toLowerCase().includes(searchQuery) ||
            device.id.toLowerCase().includes(searchQuery)
        );
    });
}

export default function Devices(props: Readonly<DeviceProps>){
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useState<string>("");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/devices/add-new-device`);
    }


    const filteredDevices = filterDevices(props.devices, searchQuery);

    return(
        <>
            <h2>Devices</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    language={props.language}
                />
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Device"][props.language]}</button>
            </div>

            {showNoPermission && (
                <NoPermissionPopup
                    language={props.language}
                    onClose={() => setShowNoPermission(false)}
                />
            )}


            <div className="device-card-container">
                {filteredDevices.map((device) => (
                    <DeviceCard key={device.id} device={device} language={props.language} />
                ))}
            </div>
        </>
    )
}
