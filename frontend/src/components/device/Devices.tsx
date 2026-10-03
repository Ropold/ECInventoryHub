import type {DeviceModel} from "../models/DeviceModel.ts";
import {useState} from "react";
import {useAutoScrollToTop, useSessionState} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import DeviceCard from "./DeviceCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";
import {
    type DeviceStatusFilter,
    type DeviceTypeFilter,
    deviceStatusFilters,
    deviceTypeFilters
} from "../utils/DeviceFilters.ts";

type DeviceProps = {
    language: string;
    role: string;
    devices: DeviceModel[];
}

function filterDevices(
    devices: DeviceModel[],
    query: string,
    statusFilter: DeviceStatusFilter,
    typeFilter: DeviceTypeFilter
): DeviceModel[] {
    if (!devices) return [];

    const searchQuery = query.toLowerCase();

    return devices.filter(device => {
        if (statusFilter !== "ALL" && device.status !== statusFilter) return false;
        if (typeFilter !== "ALL" && device.type !== typeFilter) return false;
        return (
            device.manufacturer?.toLowerCase().includes(searchQuery) ||
            device.modelName?.toLowerCase().includes(searchQuery) ||
            device.serialNumber?.toLowerCase().includes(searchQuery) ||
            device.inventoryNumber?.toLowerCase().includes(searchQuery) ||
            device.hostname?.toLowerCase().includes(searchQuery) ||
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

    const [searchQuery, setSearchQuery] = useSessionState<string>("devices.searchQuery", "");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);
    const [statusFilter, setStatusFilter] = useSessionState<DeviceStatusFilter>("devices.statusFilter", "AVAILABLE");
    const [typeFilter, setTypeFilter] = useSessionState<DeviceTypeFilter>("devices.typeFilter", "ALL");

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/devices/add-new-device`);
    }


    const filteredDevices = filterDevices(props.devices, searchQuery, statusFilter, typeFilter);

    return(
        <>
            <h2>Devices</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    language={props.language}
                    hasActiveFilters={statusFilter !== "AVAILABLE" || typeFilter !== "ALL"}
                    onReset={() => {
                        setStatusFilter("AVAILABLE");
                        setTypeFilter("ALL");
                    }}
                />
                <select
                    className="filter-select"
                    value={typeFilter}
                    onChange={(e) => setTypeFilter(e.target.value as DeviceTypeFilter)}
                >
                    {deviceTypeFilters.map((filter) => (
                        <option key={filter.value} value={filter.value}>
                            {translatedInfo[filter.labelKey][props.language]}
                        </option>
                    ))}
                </select>
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Device"][props.language]}</button>
            </div>

            <div className="filter-row">
                <div className="filter-toggle" role="group">
                    {deviceStatusFilters.map((filter) => (
                        <button
                            key={filter.value}
                            type="button"
                            className={statusFilter === filter.value ? "button-blue" : "button-grey"}
                            aria-pressed={statusFilter === filter.value}
                            onClick={() => setStatusFilter(filter.value)}
                        >
                            {translatedInfo[filter.labelKey][props.language]}
                        </button>
                    ))}
                </div>
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
